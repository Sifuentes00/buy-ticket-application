import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { hasRole } from '../keycloak';
import { apiGet, apiPost, apiPut, apiDelete } from '../api';
import type { AuthUser } from '../types';

import {
    Typography,
    CircularProgress,
    Box,
    IconButton,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Grid,
    Card,
    CardContent,
    CardActions,
    Divider,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    useTheme,
    useMediaQuery
} from '@mui/material';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';
import EditIcon from '@mui/icons-material/Edit';
import TheatersIcon from '@mui/icons-material/Theaters';

interface MoviesTableProps {
    currentUser: AuthUser;
}

interface Movie {
    id: number;
    title: string;
    director: string;
    releaseYear: number;
    genre: string;
    reviews?: Array<{ id: number; rating: number }>;
    showtimes?: Array<{ id: number }>;
}

interface DialogFormData {
    id?: number;
    title: string;
    director: string;
    releaseYear: string;
    genre: string;
}

interface DialogFormErrors {
    title?: string;
    director?: string;
    releaseYear?: string;
    genre?: string;
}

function MoviesTable({ currentUser }: MoviesTableProps) {
    const navigate = useNavigate();
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const [movies, setMovies] = useState<Movie[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [isModalOpen, setModalOpen] = useState(false);
    const [dialogFormData, setDialogFormData] = useState<DialogFormData>({
        title: '', director: '', releaseYear: '', genre: '',
    });

    const [favoriteIds, setFavoriteIds] = useState<Set<number>>(new Set());

    const [dialogFormErrors, setDialogFormErrors] = useState<DialogFormErrors>({});

    const calculateAverageRating = (reviews?: Movie['reviews']): number | null => {
        if (!reviews || reviews.length === 0) return null;
        const validRatings = reviews.filter(
            review => review.rating !== undefined && review.rating !== null && typeof review.rating === 'number'
        );
        if (validRatings.length === 0) return null;
        const totalRating = validRatings.reduce((sum, review) => sum + review.rating, 0);
        return totalRating / validRatings.length;
    };

    const getRatingColor = (averageRating: number): string => {
        if (averageRating >= 8) return '#4caf50';
        if (averageRating >= 5) return '#ffeb3b';
        return '#f44336';
    };

    useEffect(() => {
        fetchMovies();
        if (currentUser) {
            fetchFavorites();
        } else {
            setFavoriteIds(new Set());
        }
    }, [currentUser]);

    const fetchFavorites = () => {
        apiGet<Movie[]>(`/favorites?userId=${currentUser?.id}`)
            .then(res => {
                const ids = new Set(res.data.map(m => m.id));
                setFavoriteIds(ids);
            })
            .catch(err => console.error("Ошибка загрузки избранного", err));
    };

    const handleToggleFavorite = async (movieId: number) => {
        if (!currentUser) {
            alert("Войдите в систему, чтобы добавлять в избранное!");
            return;
        }

        const isFav = favoriteIds.has(movieId);
        try {
            if (isFav) {
                await apiDelete(`/favorites?userId=${currentUser.id}&movieId=${movieId}`);
                setFavoriteIds(prev => {
                    const next = new Set(prev);
                    next.delete(movieId);
                    return next;
                });
            } else {
                await apiPost(`/favorites?userId=${currentUser.id}&movieId=${movieId}`, {});
                setFavoriteIds(prev => new Set(prev).add(movieId));
            }
        } catch (err) {
            console.error("Ошибка при изменении избранного", err);
        }
    };

    const fetchMovies = () => {
        setLoading(true);

        apiGet<any>('/movies')
            .then(response => {
                const data = response.data;
                const moviesArray = Array.isArray(data) ? data : data.content || data.movies || [];

                const formattedMovies = moviesArray.map((movie: any) => ({
                    ...movie,
                    reviews: movie.reviews || [],
                    showtimes: movie.showtimes || []
                }));

                setMovies(formattedMovies);
                setLoading(false);
                setError(null);
            })
            .catch(err => {
                console.error("Error fetching movies:", err);
                setError('Не удалось загрузить фильмы.');
                setLoading(false);
            });
    };

    const handleDelete = (id: number) => {
        if (window.confirm(`Вы уверены, что хотите удалить фильм?`)) {
            apiDelete(`/movies/${id}`)
                .then(() => {
                    setMovies(movies.filter(movie => movie.id !== id));
                })
                .catch(err => {
                    console.error(`Ошибка при удалении фильма:`, err);
                    alert('Не удалось удалить фильм.');
                });
        }
    };

    const handleDetailsClick = (movieId: number) => {
        navigate(`/movies/${movieId}`);
    };

    const handleOpenModal = (movie?: Movie) => {
        if (movie) {
            setDialogFormData({
                id: movie.id,
                title: movie.title,
                director: movie.director,
                releaseYear: movie.releaseYear.toString(),
                genre: movie.genre,
            });
        } else {
            setDialogFormData({
                id: undefined,
                title: '', director: '', releaseYear: '', genre: '',
            });
        }
        setDialogFormErrors({});
        setModalOpen(true);
    };

    const handleCloseModal = () => {
        setModalOpen(false);
        setDialogFormData({
            id: undefined,
            title: '', director: '', releaseYear: '', genre: '',
        });
        setDialogFormErrors({});
    };

    const handleDialogInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;
        setDialogFormData(prevState => ({ ...prevState, [name]: value }));
        setDialogFormErrors(prevErrors => ({ ...prevErrors, [name]: undefined }));
    };

    const validateDialogForm = (): DialogFormErrors => {
        const errors: DialogFormErrors = {};
        const currentYear = new Date().getFullYear();
        const { title, director, releaseYear, genre } = dialogFormData;

        if (!title.trim()) errors.title = 'Название не может быть пустым';
        if (!director.trim()) errors.director = 'Режиссер не может быть пустым';

        if (!releaseYear.trim()) {
            errors.releaseYear = 'Год выхода должен быть числом';
        } else {
            const parsedYear = parseInt(releaseYear, 10);
            if (isNaN(parsedYear)) {
                errors.releaseYear = 'Введите корректный год (число)';
            } else if (parsedYear < 1895 || parsedYear > currentYear + 5) {
                errors.releaseYear = `Год должен быть от 1895 до ${currentYear + 5}`;
            }
        }

        if (!genre.trim()) errors.genre = 'Жанр не может быть пустым';

        return errors;
    };

    const handleSaveDialogForm = () => {
        const errors = validateDialogForm();
        if (Object.keys(errors).length > 0) {
            setDialogFormErrors(errors);
            return;
        }

        const movieDataToSend = {
            title: dialogFormData.title.trim(),
            director: dialogFormData.director.trim(),
            releaseYear: parseInt(dialogFormData.releaseYear, 10),
            genre: dialogFormData.genre.trim(),
        };

        const isEditing = dialogFormData.id !== undefined;
        const apiCall = isEditing
            ? apiPut<Movie>(`/movies/${dialogFormData.id}`, movieDataToSend)
            : apiPost<Movie>('/movies', movieDataToSend);

        apiCall
            .then(() => {
                fetchMovies();
                handleCloseModal();
            })
            .catch(err => {
                const errorMessage = err.response?.data?.message || 'Неизвестная ошибка';
                alert(`Не удалось сохранить фильм.\nОшибка: ${errorMessage}`);
            });
    };

    const modalTitle = dialogFormData.id !== undefined ? 'Редактировать фильм' : 'Добавить новый фильм';
    const modalSubmitButtonText = dialogFormData.id !== undefined ? 'Сохранить изменения' : 'Добавить фильм';
    const textColor = '#ffffff';

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
                <Typography variant="h6" sx={{ ml: 2 }}>Загрузка фильмов...</Typography>
            </Box>
        );
    }

    if (error) {
        return <Typography color="error" sx={{ mt: 4, textAlign: 'center' }}>{error}</Typography>;
    }

    // --- РЕНДЕР ДЛЯ МОБИЛОК (Карточки) ---
    const renderMobileView = () => (
        <Grid container spacing={3}>
            {movies.map((movie) => {
                const averageRating = calculateAverageRating(movie.reviews);
                const hasNumericRating = typeof averageRating === 'number';
                const ratingColor = hasNumericRating ? getRatingColor(averageRating!) : '#bdbdbd';

                return (
                    <Grid size={{ xs: 12, sm: 6 }} key={movie.id}>
                        <Card
                            sx={{
                                height: '100%',
                                display: 'flex',
                                flexDirection: 'column',
                                bgcolor: '#212121',
                                color: '#ffffff',
                                transition: 'transform 0.2s',
                                '&:hover': { transform: 'translateY(-4px)', boxShadow: 6 }
                            }}
                        >
                            <CardContent sx={{ flexGrow: 1 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                                    <Typography variant="h6" component="div" sx={{ fontWeight: 'bold', lineHeight: 1.2 }}>
                                        {movie.title}
                                    </Typography>

                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <IconButton
                                            size="small"
                                            onClick={(e) => { e.stopPropagation(); handleToggleFavorite(movie.id); }}
                                            sx={{ color: '#ff4081' }}
                                        >
                                            {favoriteIds.has(movie.id) ? <FavoriteIcon /> : <FavoriteBorderIcon />}
                                        </IconButton>

                                        <Box sx={{ display: 'flex', alignItems: 'center', bgcolor: '#303030', px: 1, py: 0.5, borderRadius: 1 }}>
                                            <StarIcon sx={{ fontSize: '1rem', color: ratingColor, mr: 0.5 }} />
                                            <Typography variant="body2" sx={{ color: ratingColor, fontWeight: 'bold' }}>
                                                {hasNumericRating ? averageRating!.toFixed(1) : '—'}
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Box>
                                <Typography variant="body2" sx={{ color: '#bdbdbd', mb: 1 }}>Режиссер: {movie.director}</Typography>
                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                                    <Typography variant="caption" sx={{ bgcolor: '#424242', px: 1, py: 0.5, borderRadius: 1 }}>{movie.releaseYear}</Typography>
                                    <Typography variant="caption" sx={{ bgcolor: '#424242', px: 1, py: 0.5, borderRadius: 1 }}>{movie.genre}</Typography>
                                </Box>
                            </CardContent>
                            <Divider sx={{ bgcolor: '#424242' }} />
                            <CardActions sx={{ justifyContent: 'space-between', p: 2 }}>
                                <Button
                                    variant="contained" size="small" startIcon={<TheatersIcon />}
                                    onClick={() => handleDetailsClick(movie.id)}
                                >
                                    Сеансы
                                </Button>
                                {hasRole("ADMIN") && (
                                    <Box>
                                        <IconButton size="small" color="primary" onClick={() => handleOpenModal(movie)}><EditIcon fontSize="small" /></IconButton>
                                        <IconButton size="small" color="error" onClick={() => handleDelete(movie.id)}><DeleteIcon fontSize="small" /></IconButton>
                                    </Box>
                                )}
                            </CardActions>
                        </Card>
                    </Grid>
                );
            })}
        </Grid>
    );

    const renderDesktopView = () => (
        <TableContainer component={Paper} sx={{ boxShadow: 3, backgroundColor: '#212121', color: textColor, borderRadius: 1 }}>
            <Table sx={{ minWidth: 650 }} aria-label="movies table">
                <TableHead sx={{ backgroundColor: '#424242' }}>
                    <TableRow>
                        <TableCell align="center" sx={{ color: textColor }}>Название</TableCell>
                        <TableCell align="center" sx={{ color: textColor }}>Режиссер</TableCell>
                        <TableCell align="center" sx={{ width: '130px', color: textColor }}>Год выхода</TableCell>
                        <TableCell align="center" sx={{ color: textColor }}>Жанр</TableCell>
                        <TableCell align="center" sx={{ width: '110px', color: textColor }}>Рейтинг</TableCell>

                        {/* НОВАЯ КОЛОНКА ДЛЯ ИЗБРАННОГО */}
                        {currentUser && <TableCell align="center" sx={{ width: '60px', color: textColor }}></TableCell>}

                        <TableCell align="center" sx={{ width: '160px', color: textColor }}>Билеты</TableCell>
                        <TableCell align="center" sx={{ color: textColor, width: '60px' }}></TableCell>
                        <TableCell align="center" sx={{ color: textColor, width: '60px' }}></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {movies.map((movie) => {
                        const averageRating = calculateAverageRating(movie.reviews);
                        const hasNumericRating = typeof averageRating === 'number';
                        const ratingColor = hasNumericRating ? getRatingColor(averageRating!) : textColor;

                        return (
                            <TableRow key={movie.id} sx={{ '&:last-child td, &:last-child th': { border: 0 }, '&:hover': { backgroundColor: '#616161' } }}>
                                <TableCell align="center" component="th" scope="row" sx={{ color: textColor }}>{movie.title}</TableCell>
                                <TableCell align="center" sx={{ color: textColor }}>{movie.director}</TableCell>
                                <TableCell align="center" sx={{ width: '130px', color: textColor }}>{movie.releaseYear}</TableCell>
                                <TableCell align="center" sx={{ color: textColor }}>{movie.genre}</TableCell>
                                <TableCell align="center" sx={{ width: '110px', color: textColor }}>
                                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                        {hasNumericRating ? (
                                            <Typography variant="body2" component="span" sx={{ color: ratingColor, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                                <StarIcon sx={{ fontSize: 'small', verticalAlign: 'middle', mr: 0.5, color: ratingColor }} />
                                                {averageRating!.toFixed(1)}
                                            </Typography>
                                        ) : (
                                            <Typography variant="body2" component="span" sx={{ color: textColor }}>Нет оценок</Typography>
                                        )}
                                        <Typography variant="caption" sx={{ color: textColor, opacity: 0.7 }}>({movie.reviews ? movie.reviews.length : 0})</Typography>
                                    </Box>
                                </TableCell>

                                {/* КНОПКА ИЗБРАННОЕ В ТАБЛИЦЕ */}
                                {currentUser && (
                                    <TableCell align="center" sx={{ width: '60px' }}>
                                        <IconButton
                                            size="small"
                                            onClick={() => handleToggleFavorite(movie.id)}
                                            sx={{ color: '#ff4081' }}
                                        >
                                            {favoriteIds.has(movie.id) ? <FavoriteIcon /> : <FavoriteBorderIcon />}
                                        </IconButton>
                                    </TableCell>
                                )}

                                <TableCell align="center" sx={{ width: '160px', color: textColor }}>
                                    <Button variant="contained" size="small" onClick={() => handleDetailsClick(movie.id)}>БИЛЕТЫ</Button>
                                </TableCell>
                                <TableCell align="center" sx={{ width: '60px', color: textColor }}>
                                    {hasRole("ADMIN") && (
                                        <IconButton aria-label="edit" size="small" color="primary" onClick={() => handleOpenModal(movie)}>
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    )}
                                </TableCell>
                                <TableCell align="center" sx={{ width: '60px', color: textColor }}>
                                    {hasRole("ADMIN") && (
                                        <IconButton aria-label="delete" size="small" color="error" onClick={() => handleDelete(movie.id)}>
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    )}
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </TableContainer>
    );

    return (
        <Box sx={{ mt: 2, pb: 4, width: '100%', maxWidth: '1200px', margin: 'auto' }}>
            <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h4" sx={{ fontWeight: 'bold', fontSize: { xs: '1.8rem', md: '2.125rem' } }}>
                    Афиша
                </Typography>

                {hasRole("ADMIN") && (
                    <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => handleOpenModal()}>
                        Добавить фильм
                    </Button>
                )}
            </Box>

            {movies.length === 0 ? (
                <Typography sx={{ textAlign: 'center', mt: 4, color: '#bdbdbd' }}>
                    Нет данных о фильмах. Администратор может добавить первый!
                </Typography>
            ) : (
                // МАГИЯ АДАПТИВНОСТИ: Если телефон, рисуем Карточки, если ПК - Таблицу
                isMobile ? renderMobileView() : renderDesktopView()
            )}

            {/* Модальное окно */}
            <Dialog open={isModalOpen} onClose={handleCloseModal} fullWidth maxWidth="sm">
                <DialogTitle>{modalTitle}</DialogTitle>
                <DialogContent>
                    <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }} noValidate>
                        <TextField
                            autoFocus margin="dense" name="title" label="Название фильма" fullWidth
                            value={dialogFormData.title} onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.title} helperText={dialogFormErrors.title || ' '}
                        />
                        <TextField
                            margin="dense" name="director" label="Режиссер" fullWidth
                            value={dialogFormData.director} onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.director} helperText={dialogFormErrors.director || ' '}
                        />
                        <TextField
                            margin="dense" name="releaseYear" label="Год выхода" type="number" fullWidth
                            value={dialogFormData.releaseYear} onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.releaseYear} helperText={dialogFormErrors.releaseYear || ' '}
                        />
                        <TextField
                            margin="dense" name="genre" label="Жанр" fullWidth
                            value={dialogFormData.genre} onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.genre} helperText={dialogFormErrors.genre || ' '}
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseModal} color="secondary">Отмена</Button>
                    <Button
                        onClick={handleSaveDialogForm} color="primary" variant="contained"
                        disabled={!dialogFormData.title.trim() || !dialogFormData.director.trim() || !dialogFormData.releaseYear.trim() || !dialogFormData.genre.trim()}
                    >
                        {modalSubmitButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default MoviesTable;
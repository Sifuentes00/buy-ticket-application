// In file src/components/MoviesTable.tsx

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
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

} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';
import EditIcon from '@mui/icons-material/Edit';
import { useNavigate, useLocation } from 'react-router-dom';

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


function MoviesTable() {
    const navigate = useNavigate();
    const location = useLocation();

    const [movies, setMovies] = useState<Movie[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [isModalOpen, setModalOpen] = useState(false);
    const [dialogFormData, setDialogFormData] = useState<DialogFormData>({
        title: '', director: '', releaseYear: '', genre: '',
    });

    const [dialogFormErrors, setDialogFormErrors] = useState<DialogFormErrors>({});

    const API_URL = 'http://localhost:8080/api/movies';


    const calculateAverageRating = (reviews?: Movie['reviews']): number | null => {
        if (!reviews || reviews.length === 0) return null;
        const validRatings = reviews.filter(
            review => review.rating !== undefined
                && review.rating !== null
                && typeof review.rating === 'number'
        );
        if (validRatings.length === 0) return null;
        const totalRating = validRatings.reduce((sum, review) => sum + review.rating, 0);
        const average = totalRating / validRatings.length;
        return average;
    };

    const getRatingColor = (averageRating: number): string => {
        if (averageRating >= 8) return '#4caf50'; // Зеленый
        if (averageRating >= 5) return '#ffeb3b'; // Желтый
        return '#f44336'; // Красный
    };


    useEffect(() => {
        fetchMovies();
    }, [location.key]);


    const fetchMovies = () => {
        setLoading(true);
        axios.get<Movie[]>(API_URL)
            .then(response => {
                const formattedMovies = response.data.map(movie => ({
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
            axios.delete(`${API_URL}/${id}`)
                .then(() => {
                    setMovies(movies.filter(movie => movie.id !== id));
                    console.log(`Фильм с ID ${id} успешно удален.`);
                })
                .catch(err => {
                    console.error(
                        `Ошибка при удалении фильма с ID ${id}:`,
                        err.response?.data || err.message
                    );
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

    const handleDialogInputChange = (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        const { name, value } = event.target;
        setDialogFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
        setDialogFormErrors(prevErrors => ({
            ...prevErrors,
            [name]: undefined,
        }));
    };

    const validateDialogForm = (): DialogFormErrors => {
        const errors: DialogFormErrors = {};
        const { title, director, releaseYear, genre } = dialogFormData;

        if (!title.trim()) {
            errors.title = 'Название не может быть пустым';
        }
        if (!director.trim()) {
            errors.director = 'Режиссер не может быть пустым';
        }

        // Валидация года выхода
        if (!releaseYear.trim()) {
            errors.releaseYear = 'Год выхода должен быть числом';
        } else {
            const parsedYear = parseInt(releaseYear, 10);
            if (isNaN(parsedYear)) {
                errors.releaseYear = 'Введите корректный год (число)';
            } else if (parsedYear < 1895 || parsedYear > 2025) {
                errors.releaseYear = `Год должен быть от 1895 до 2025`;
            }
        }

        if (!genre.trim()) {
            errors.genre = 'Жанр не может быть пустым';
        }

        return errors;
    };


    const handleSaveDialogForm = () => {
        const errors = validateDialogForm();
        if (Object.keys(errors).length > 0) {
            setDialogFormErrors(errors);
            console.log("Фронтенд-валидация формы фильма провалена:", errors);
            return;
        }

        // === ИСПРАВЛЕНО: Не включаем 'id' в movieDataToSend для PUT запроса ===
        // ID отправляется только в URL при редактировании
        const movieDataToSend = {
            title: dialogFormData.title.trim(),
            director: dialogFormData.director.trim(),
            releaseYear: parseInt(dialogFormData.releaseYear, 10),
            genre: dialogFormData.genre.trim(),
        };
        // ====================================================================


        const isEditing = dialogFormData.id !== undefined;

        const apiCall = isEditing
            ? axios.put<Movie>(
                `${API_URL}/${dialogFormData.id}`, movieDataToSend // ID в URL
            )
            : axios.post<Movie>(API_URL, movieDataToSend); // Без ID (новый фильм)


        apiCall
            .then(() => {
                fetchMovies(); // Перезагрузка всего списка гарантирует актуальность отзывов
                handleCloseModal();
                console.log(`Фильм с ID ${dialogFormData.id || 'новый'} успешно сохранен.`); // Логирование успеха
            })
            .catch(err => {
                console.error(
                    `Ошибка при ${isEditing ? 'редактировании' : 'добавлении'} фильма:`,
                    err.response?.data || err.message
                );
                const errorMessage = err.response?.data?.message || err.response?.data || err.message || `Неизвестная ошибка при ${isEditing ? 'редактировании' : 'добавлении'} фильма.`;

                // === ДОБАВЛЕНО: Более информативное сообщение об ошибке для пользователя ===
                alert(
                    `Не удалось ${isEditing ? 'отредактировать' : 'добавить'} фильм.\n`
                    + `Ошибка: ${errorMessage}\n`
                    + `Проверьте консоль разработчика и логи бэкенда для деталей.`
                );
                // ======================================================================
            });
    };



    const tableBgColor = '#212121';
    const textColor = '#ffffff';


    const modalTitle = dialogFormData.id !== undefined
        ? 'Редактировать фильм'
        : 'Добавить новый фильм';
    const modalSubmitButtonText = dialogFormData.id !== undefined
        ? 'Сохранить изменения'
        : 'Добавить фильм';


    if (loading) {
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                mt: 4,
                maxWidth: '900px',
                width: '100%',
                margin: 'auto'
            }}>
                <CircularProgress />
                <Typography variant="h6" sx={{ ml: 2 }}>
                    Загрузка фильмов...
                </Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Typography
                color="error"
                sx={{
                    mt: 4,
                    textAlign: 'center',
                    maxWidth: '900px',
                    width: '100%',
                    margin: 'auto'
                }}
            >
                {error}
            </Typography>
        );
    }

    if (movies.length === 0 && !loading && !error) {
        return (
            <Box sx={{
                mt: 0,
                px: 0,
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                maxWidth: '900px',
                width: '100%',
                margin: 'auto'
            }}>
                <Box sx={{
                    mb: 2,
                    display: 'flex',
                    justifyContent: 'flex-start',
                    width: '100%'
                }}>
                    <Button
                        variant="contained"
                        color="primary"
                        startIcon={<AddIcon />}
                        onClick={() => handleOpenModal()}
                    >
                        Добавить фильм
                    </Button>
                </Box>
                <Typography color={textColor}>
                    Нет данных о фильмов. Вы можете добавить первый!
                </Typography>

                <Dialog open={isModalOpen} onClose={handleCloseModal}>
                    <DialogTitle>{modalTitle}</DialogTitle>
                    <DialogContent>
                        <Box
                            component="form"
                            sx={{
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 2,
                                mt: 1
                            }}
                            noValidate
                            autoComplete="off"
                        >
                            <TextField
                                autoFocus
                                margin="dense"
                                name="title"
                                label="Название фильма"
                                type="text"
                                fullWidth
                                variant="outlined"
                                value={dialogFormData.title}
                                onChange={handleDialogInputChange}
                                error={!!dialogFormErrors.title}
                                helperText={dialogFormErrors.title || ' '}
                            />
                            <TextField
                                margin="dense"
                                name="director"
                                label="Режиссер"
                                type="text"
                                fullWidth
                                variant="outlined"
                                value={dialogFormData.director}
                                onChange={handleDialogInputChange}
                                error={!!dialogFormErrors.director}
                                helperText={dialogFormErrors.director || ' '}
                            />
                            <TextField
                                margin="dense"
                                name="releaseYear"
                                label="Год выхода"
                                type="number"
                                fullWidth
                                variant="outlined"
                                value={dialogFormData.releaseYear}
                                onChange={handleDialogInputChange}
                                error={!!dialogFormErrors.releaseYear}
                                helperText={dialogFormErrors.releaseYear || ' '}
                            />
                            <TextField
                                margin="dense"
                                name="genre"
                                label="Жанр"
                                type="text"
                                fullWidth
                                variant="outlined"
                                value={dialogFormData.genre}
                                onChange={handleDialogInputChange}
                                error={!!dialogFormErrors.genre}
                                helperText={dialogFormErrors.genre || ' '}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseModal} color="secondary">
                            Отмена
                        </Button>
                        <Button
                            onClick={handleSaveDialogForm}
                            color="primary"
                            variant="contained"
                        >
                            {modalSubmitButtonText}
                        </Button>
                    </DialogActions>
                </Dialog>

            </Box>
        );
    }


    return (
        <Box sx={{
            mt: 0,
            px: 0,
            maxWidth: '900px',
            width: '100%',
            margin: 'auto'
        }}>
            <Box sx={{
                mb: 2,
                display: 'flex',
                justifyContent: 'flex-start',
                width: '100%'
            }}>
                <Button
                    variant="contained"
                    color="primary"
                    startIcon={<AddIcon />}
                    onClick={() => handleOpenModal()}
                >
                    Добавить фильм
                </Button>
            </Box>


            <TableContainer
                component={Paper}
                sx={{
                    boxShadow: 3,
                    backgroundColor: tableBgColor,
                    color: textColor,
                }}
            >
                <Table sx={{ minWidth: 650 }} aria-label="movies table">
                    <TableHead sx={{ backgroundColor: '#424242' }}>
                        <TableRow>
                            {/* === Ячейки заголовков данных === */}
                            <TableCell align="center" sx={{ color: textColor }}>
                                Название
                            </TableCell>
                            <TableCell align="center" sx={{ color: textColor }}>
                                Режиссер
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    width: '130px',
                                    color: textColor
                                }}
                            >
                                Год выхода
                            </TableCell>
                            <TableCell align="center" sx={{ color: textColor }}>
                                Жанр
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    width: '110px',
                                    color: textColor
                                }}
                            >
                                Рейтинг
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    width: '160px',
                                    color: textColor
                                }}
                            >
                                Билеты
                            </TableCell>
                            {/* === Ячейки заголовков действий (перемещены в конец) === */}
                            <TableCell
                                align="center"
                                sx={{
                                    color: textColor,
                                    width: '60px'
                                }}
                            >
                                {/* Ред. */}
                            </TableCell>
                            <TableCell
                                align="center"
                                sx={{
                                    color: textColor,
                                    width: '60px'
                                }}
                            >
                                {/* Удл. */}
                            </TableCell>
                            {/* ======================================================= */}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {movies.map((movie) => {
                            const averageRating = calculateAverageRating(movie.reviews);
                            const hasNumericRating = typeof averageRating === 'number';
                            const ratingColor = hasNumericRating
                                ? getRatingColor(averageRating!)
                                : textColor;

                            return (
                                <TableRow
                                    key={movie.id}
                                    sx={{
                                        '&:last-child td, &:last-child th': { border: 0 },
                                        '&:hover': { backgroundColor: '#616161' }
                                    }}
                                >
                                    {/* Ячейки с данными фильма */}
                                    <TableCell align="center" component="th" scope="row"
                                               sx={{ color: textColor }}>
                                        {movie.title}
                                    </TableCell>
                                    <TableCell align="center"
                                               sx={{ color: textColor }}>
                                        {movie.director}
                                    </TableCell>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '130px',
                                            color: textColor
                                        }}
                                    >
                                        {movie.releaseYear}
                                    </TableCell>
                                    <TableCell align="center" sx={{ color: textColor }}>
                                        {movie.genre}
                                    </TableCell>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '110px',
                                            color: textColor
                                        }}
                                    >
                                        <Box sx={{
                                            display: 'flex',
                                            flexDirection: 'column',
                                            alignItems: 'center'
                                        }}>
                                            {hasNumericRating ? (
                                                <Typography
                                                    variant="body2"
                                                    component="span"
                                                    sx={{
                                                        color: ratingColor,
                                                        fontWeight: 'bold',
                                                        display: 'flex',
                                                        alignItems: 'center'
                                                    }}
                                                >
                                                    <StarIcon
                                                        sx={{
                                                            fontSize: 'small',
                                                            verticalAlign: 'middle',
                                                            mr: 0.5,
                                                            color: ratingColor
                                                        }}
                                                    />
                                                    {averageRating!.toFixed(1)}
                                                </Typography>
                                            ) : (
                                                <Typography
                                                    variant="body2"
                                                    component="span"
                                                    sx={{ color: textColor }}
                                                >
                                                    Нет оценок
                                                </Typography>
                                            )}
                                            <Typography
                                                variant="caption"
                                                sx={{ color: textColor, opacity: 0.7 }}
                                            >
                                                ({movie.reviews ? movie.reviews.length : 0})
                                            </Typography>
                                        </Box>
                                    </TableCell>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '160px',
                                            color: textColor
                                        }}
                                    >
                                        <Button
                                            variant="contained"
                                            size="small"
                                            onClick={() => handleDetailsClick(movie.id)}
                                        >
                                            БИЛЕТЫ
                                        </Button>
                                    </TableCell>
                                    {/* === Ячейки для Редактирования и Удаления (перемещены в конец) === */}
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '60px',
                                            color: textColor
                                        }}
                                    >
                                        <IconButton
                                            aria-label="edit"
                                            size="small"
                                            color="primary"
                                            onClick={() => handleOpenModal(movie)}
                                        >
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    </TableCell>
                                    <TableCell
                                        align="center"
                                        sx={{
                                            width: '60px',
                                            color: textColor
                                        }}
                                    >
                                        <IconButton
                                            aria-label="delete"
                                            size="small"
                                            color="error"
                                            onClick={() => handleDelete(movie.id)}
                                        >
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    </TableCell>
                                    {/* ================================================================ */}
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>

            <Dialog open={isModalOpen} onClose={handleCloseModal}>
                <DialogTitle>{modalTitle}</DialogTitle>
                <DialogContent>
                    <Box
                        component="form"
                        sx={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: 2,
                            mt: 1
                        }}
                        noValidate
                        autoComplete="off"
                    >
                        <TextField
                            autoFocus
                            margin="dense"
                            name="title"
                            label="Название фильма"
                            type="text"
                            fullWidth
                            variant="outlined"
                            value={dialogFormData.title}
                            onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.title}
                            helperText={dialogFormErrors.title || ' '}
                        />
                        <TextField
                            margin="dense"
                            name="director"
                            label="Режиссер"
                            type="text"
                            fullWidth
                            variant="outlined"
                            value={dialogFormData.director}
                            onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.director}
                            helperText={dialogFormErrors.director || ' '}
                        />
                        <TextField
                            margin="dense"
                            name="releaseYear"
                            label="Год выхода"
                            type="number"
                            fullWidth
                            variant="outlined"
                            value={dialogFormData.releaseYear}
                            onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.releaseYear}
                            helperText={dialogFormErrors.releaseYear || ' '}
                        />
                        <TextField
                            margin="dense"
                            name="genre"
                            label="Жанр"
                            type="text"
                            fullWidth
                            variant="outlined"
                            value={dialogFormData.genre}
                            onChange={handleDialogInputChange}
                            error={!!dialogFormErrors.genre}
                            helperText={dialogFormErrors.genre || ' '}
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseModal} color="secondary">
                        Отмена
                    </Button>
                    <Button
                        onClick={handleSaveDialogForm}
                        color="primary"
                        variant="contained"
                        // === ИСПРАВЛЕНО: Добавлено условие для блокировки кнопки ===
                        disabled={
                            // Блокируем, если любое из обязательных полей пустое (после trim)
                            !dialogFormData.title.trim() ||
                            !dialogFormData.director.trim() ||
                            !dialogFormData.releaseYear.trim() || // Год тоже обязателен
                            !dialogFormData.genre.trim()
                        }
                    >
                        {modalSubmitButtonText}
                    </Button>
                </DialogActions>
            </Dialog>

        </Box>
    );
}

export default MoviesTable;

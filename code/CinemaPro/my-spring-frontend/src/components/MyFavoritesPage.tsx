import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiGet, apiDelete } from '../api';
import type { AuthUser } from '../types';
import {
    Typography, Box, CircularProgress, Grid, Card, CardContent,
    CardActions, Divider, Button, IconButton
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import TheatersIcon from '@mui/icons-material/Theaters';
import FavoriteIcon from '@mui/icons-material/Favorite';

interface Movie {
    id: number;
    title: string;
    director: string;
    releaseYear: number;
    genre: string;
    reviews?: Array<{ id: number; rating: number }>;
}

export default function MyFavoritesPage({ currentUser }: { currentUser: AuthUser }) {
    const navigate = useNavigate();
    const [movies, setMovies] = useState<Movie[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (currentUser) {
            // Исправлено: убрали userId из параметров
            apiGet<Movie[]>('/favorites')
                .then(res => setMovies(res.data))
                .finally(() => setLoading(false));
        }
    }, [currentUser]);

    const handleRemoveFavorite = async (movieId: number) => {
        try {
            // Исправлено: убрали userId из параметров
            await apiDelete(`/favorites?movieId=${movieId}`);
            setMovies(movies.filter(m => m.id !== movieId));
        } catch (err) {
            console.error("Ошибка при удалении", err);
        }
    };

    if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>;

    return (
        <Box sx={{ mt: 2, pb: 4, px: 2, maxWidth: '1200px', margin: 'auto' }}>
            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 4, fontSize: { xs: '1.8rem', md: '2.125rem' } }}>
                Мое избранное
            </Typography>

            {movies.length === 0 ? (
                <Typography sx={{ textAlign: 'center', color: '#bdbdbd' }}>У вас пока нет любимых фильмов.</Typography>
            ) : (
                <Grid container spacing={3}>
                    {movies.map(movie => {
                        const totalRating = movie.reviews?.reduce((sum, r) => sum + r.rating, 0) || 0;
                        const avgRating = movie.reviews?.length ? (totalRating / movie.reviews.length).toFixed(1) : '—';

                        let ratingColor = '#bdbdbd';
                        if (avgRating !== '—') {
                            const numRating = parseFloat(avgRating);
                            if (numRating >= 8) ratingColor = '#4caf50';
                            else if (numRating >= 5) ratingColor = '#ffeb3b';
                            else ratingColor = '#f44336';
                        }

                        return (
                            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={movie.id}>
                                <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: '#212121', color: '#ffffff' }}>
                                    <CardContent sx={{ flexGrow: 1 }}>
                                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, alignItems: 'flex-start' }}>
                                            <Typography variant="h6" sx={{ fontWeight: 'bold', lineHeight: 1.2 }}>
                                                {movie.title}
                                            </Typography>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                <IconButton size="small" onClick={() => handleRemoveFavorite(movie.id)}>
                                                    <FavoriteIcon sx={{ color: '#ff4081' }} />
                                                </IconButton>
                                                <Box sx={{ display: 'flex', alignItems: 'center', bgcolor: '#303030', px: 1, py: 0.5, borderRadius: 1 }}>
                                                    <StarIcon sx={{ fontSize: '1rem', color: ratingColor, mr: 0.5 }} />
                                                    <Typography variant="body2" sx={{ color: ratingColor, fontWeight: 'bold' }}>
                                                        {avgRating}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        </Box>
                                        <Typography variant="body2" sx={{ color: '#bdbdbd', mb: 1 }}>Режиссер: {movie.director}</Typography>
                                        <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                                            <Typography variant="caption" sx={{ bgcolor: '#424242', px: 1, py: 0.5, borderRadius: 1 }}>{movie.releaseYear}</Typography>
                                        </Box>
                                    </CardContent>
                                    <Divider sx={{ bgcolor: '#424242' }} />
                                    <CardActions>
                                        <Button variant="contained" size="small" startIcon={<TheatersIcon />} onClick={() => navigate(`/movies/${movie.id}`)}>
                                            Сеансы
                                        </Button>
                                    </CardActions>
                                </Card>
                            </Grid>
                        );
                    })}
                </Grid>
            )}
        </Box>
    );
}
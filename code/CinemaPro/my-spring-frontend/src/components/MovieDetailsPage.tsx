import React, { useEffect, useState } from 'react'; // УДАЛЕНО: ReactNode
import { useParams } from 'react-router-dom'; // Импортируем useNavigate
import axios from 'axios';
import {
    Container,
    Typography,
    Box,
    CircularProgress,
    Paper,
    Divider,
    List,
    ListItem,
    ListItemText,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    IconButton,
    Rating,
    FormControl,
    InputLabel,
    Select,
    MenuItem
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select'; // <-- ИСПРАВЛЕНО: Импортируем SelectChangeEvent как тип
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';
import AccessTimeIcon from '@mui/icons-material/AccessTime'; // Иконка для времени

interface Movie {
    id: number;
    title: string;
    director: string;
    releaseYear: number;
    genre: string;
    reviews?: Array<Review>;
}

interface Showtime {
    id: number;
    dateTime: string;
    hall: string;
    type?: string;
    movieId?: number;
    theaterId?: number;
}

interface Ticket {
    id: number;
    seatNumber: string;
}


interface MovieFormData {
    id?: number;
    title: string;
    director: string;
    releaseYear: string;
    genre: string;
}

interface ShowtimeFormData {
    id?: number;
    movieId: number;
    dateTime: string;
    type: string;
}

interface Review {
    id: number;
    rating: number;
    content?: string;
    user: { id: number; username: string; } | null;
    movie: { id: number; } | null;
}

interface ReviewFormData {
    id?: number;
    movieId: number;
    userId?: number;
    rating: number;
    content: string;
}

interface User {
    id: number;
    username: string;
    email: string;
}

interface MovieDetailsPageProps {
    currentUser: User | null;
}

// --- КОНСТАНТЫ ---
const ROWS = 5;
const SEATS_PER_ROW = 10;
const SEAT_PRICE = 300;

// --- URL'ы бэкенда ---
const MOVIES_API_URL = 'http://localhost:8080/api/movies';
const SHOWTIMES_API_URL = 'http://localhost:8080/api/showtimes';
const SHOWTIMES_BY_MOVIE_API_URL_BASE = 'http://localhost:8080/api/showtimes/movie';
const REVIEWS_API_URL = 'http://localhost:8080/api/reviews';
const REVIEWS_BY_MOVIE_API_URL_BASE = 'http://localhost:8080/api/reviews/movie';
const TICKETS_BY_SHOWTIME_API_URL_BASE = `http://localhost:8080/api/tickets/showtime`;
const PURCHASE_TICKET_API_URL = `http://localhost:8080/api/tickets/purchase`;


function MovieDetailsPage({ currentUser }: MovieDetailsPageProps) {
    const { id } = useParams<{ id: string }>();
    const movieId = id ? parseInt(id, 10) : null;

    const [movie, setMovie] = useState<Movie | null>(null);
    const [showtimes, setShowtimes] = useState<Showtime[]>([]);
    const [reviews, setReviews] = useState<Review[]>([]);

    const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null);

    const [showtimeTickets, setShowtimeTickets] = useState<Ticket[]>([]);
    const [ticketsLoading, setTicketsLoading] = useState(false);
    const [ticketsError, setTicketsError] = useState<string | null>(null);

    const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
    const [totalPrice, setTotalPrice] = useState(0);
    const [isPurchasing, setIsPurchasing] = useState(false);


    const [loading, setLoading] = useState(true);
    const [showtimesLoading, setShowtimesLoading] = useState(false);
    const [reviewsLoading, setReviewsLoading] = useState(false);

    const [error, setError] = useState<string | null>(null);
    const [showtimesError, setShowtimesError] = useState<string | null>(null);
    const [reviewsError, setReviewsError] = useState<string | null>(null);


    const [isShowtimeModalOpen, setIsShowtimeModalOpen] = useState(false);
    const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
    const [isMovieModalOpen, setIsMovieModalOpen] = useState(false);
    const [isTicketModalOpen, setIsTicketModalOpen] = useState(false);


    const [showtimeFormData, setShowtimeFormData] = useState<ShowtimeFormData>({ movieId: movieId || 0, dateTime: '', type: '' });
    const [reviewFormData, setReviewFormData] = useState<ReviewFormData>({ movieId: movieId || 0, rating: 0, content: '' });
    const [dialogFormData, setDialogFormData] = useState<MovieFormData>({ id: undefined, title: '', director: '', releaseYear: '', genre: '' });


    const [isSubmittingShowtime, setIsSubmittingShowtime] = useState(false);
    const [isSubmittingReview, setIsSubmittingReview] = useState(false);
    const [isSubmittingMovie, setIsSubmittingMovie] = useState(false);


    const calculateAverageRating = (reviewsToCalculate?: Review[]): number | null => {
        const reviewsList = reviewsToCalculate || reviews;
        if (!reviewsList || reviewsList.length === 0) return null;
        const validRatings = reviewsList.filter(r => r.rating !== undefined && r.rating !== null && typeof r.rating === 'number' && r.rating >= 1 && r.rating <= 10);
        if (validRatings.length === 0) return null;
        const totalRating = validRatings.reduce((sum, r) => sum + r.rating, 0);
        return totalRating / validRatings.length;
    };

    const getRatingColor = (rating: number): string => {
        if (rating >= 8) return '#4caf50';
        if (rating >= 5) return '#ffeb3b';
        return '#f44336';
    };


    const fetchReviewsForMovie = async (id: number | null) => {
        if (id === null) {
            setReviews([]);
            setReviewsError(null);
            setReviewsLoading(false);
            return;
        }
        const url = `${REVIEWS_BY_MOVIE_API_URL_BASE}/${id}`;

        setReviewsLoading(true);
        setReviewsError(null);

        try {
            const response = await axios.get<Review[]>(url);

            if (!Array.isArray(response.data)) {
                if (response.status === 204) {
                    setReviews([]);
                    setReviewsLoading(false);
                    setReviewsError(null);
                    return;
                }
                throw new Error("Некорректный формат данных отзывов.");
            }

            const formattedReviews: Review[] = response.data.map(review => ({
                ...review,
                user: review.user ? { id: review.user.id, username: review.user.username } : null,
                movie: review.movie ? { id: review.movie.id } : null,
            }));


            setReviews(formattedReviews);
            setReviewsLoading(false);
            setReviewsError(null);

        } catch (err: any) {
            const errorMessage = err.message || err.response?.data?.message || err.response?.data || err;
            console.error(`Error fetching reviews for movie ID ${id}:`, errorMessage);
            setReviews([]);
            setReviewsError('Не удалось загрузить отзывы.');
            setReviewsLoading(false);
        }
    };

    const fetchShowtimesForMovie = async (id: number | null) => {
        setSelectedShowtime(null);

        if (id === null) {
            setShowtimes([]);
            setShowtimesError(null);
            setShowtimesLoading(false);
            return;
        }
        const url = `${SHOWTIMES_BY_MOVIE_API_URL_BASE}/${id}`;

        setShowtimesLoading(true);
        setShowtimesError(null);

        try {
            const response = await axios.get<Showtime[]>(url);

            if (!Array.isArray(response.data)) {
                if (response.status === 204) {
                    setShowtimes([]);
                    setShowtimesLoading(false);
                    setShowtimesError(null);
                    return;
                }
                throw new Error("Некорректный формат данных сеансов.");
            }

            setShowtimes(response.data);
            setShowtimesLoading(false);
            setShowtimesError(null);

        } catch (err: any) {
            const errorMessage = err.message || err.response?.data?.message || err.response?.data || err;
            console.error(`Error fetching showtimes for movie ID ${id}:`, errorMessage);
            setShowtimes([]);
            setShowtimesError('Не удалось загрузить расписание сеансов.');
            setShowtimesLoading(false);
        }
    };

    const fetchTicketsForShowtime = async (showtimeId: number) => {
        const url = `${TICKETS_BY_SHOWTIME_API_URL_BASE}/${showtimeId}`;

        setTicketsLoading(true);
        setTicketsError(null);

        try {
            const response = await axios.get<Ticket[]>(url);

            if (response.status === 204) {
                setShowtimeTickets([]);
                setTicketsLoading(false);
                setTicketsError(null);
                return;
            }

            if (!Array.isArray(response.data)) {
                if (response.data === null || response.data === undefined) {
                    setShowtimeTickets([]);
                    setTicketsLoading(false);
                    setTicketsError(null);
                    return;
                }
                throw new Error("Некорректный формат данных билетов.");
            }

            const validTickets = response.data.filter(ticket => typeof ticket.seatNumber === 'string' && ticket.seatNumber.trim() !== '');
            setShowtimeTickets(validTickets);
            setTicketsLoading(false);
            setTicketsError(null);

        } catch (err: any) {
            const errorMessage = err.message || err.response?.data?.message || err.response?.data || err;
            console.error(`Error fetching tickets for showtime ID ${showtimeId}:`, errorMessage);
            setShowtimeTickets([]);
            setTicketsError('Не удалось загрузить информацию о занятых местах.');
            setTicketsLoading(false);
        }
    };

    const handlePurchaseTickets = async () => {
        if (!selectedShowtime || selectedSeats.length === 0) {
            alert("Пожалуйста, выберите сеанс и хотя бы одно место.");
            return;
        }

        if (!currentUser || !currentUser.id) {
            console.error("Current user is not logged in or user ID is missing.", currentUser);
            alert("Пожалуйста, войдите или зарегистрируйтесь, чтобы купить билеты.");
            return;
        }

        setIsPurchasing(true);

        const purchaseData = {
            showtimeId: selectedShowtime.id,
            seatNumbers: selectedSeats,
            userId: currentUser.id,
        };

        try {
            await axios.post(PURCHASE_TICKET_API_URL, purchaseData);
            alert(`Поздравляем! Вы успешно купили ${selectedSeats.length} билет(ов)!`);

            if (selectedShowtime.id) {
                fetchTicketsForShowtime(selectedShowtime.id);
            }

            handleCloseTicketModal();

        } catch (err: any) {
            const errorMessage = err.response?.data?.message
                || err.response?.data
                || err.message
                || "Неизвестная ошибка при покупке.";

            console.error("Ошибка при покупке билетов:", err.response?.data || err.message || err);
            alert(`Не удалось купить билеты: ${typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage)}`);

        } finally {
            setIsPurchasing(false);
        }
    };


    // --- Эффект для загрузки данных при монтировании компонента или изменении ID фильма ---
    useEffect(() => {
        if (movieId === null) {
            setError("Некорректный ID фильма.");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);
        setShowtimesLoading(false);
        setShowtimesError(null);
        setReviewsLoading(false);
        setReviewsError(null);
        setMovie(null);
        setShowtimes([]);
        setReviews([]);
        setSelectedShowtime(null);

        axios.get<Movie>(`${MOVIES_API_URL}/${movieId}`)
            .then(response => {
                setMovie(response.data);
                setLoading(false);
                setError(null);
            })
            .catch(err => {
                const errorMessage = err.message || err.response?.data?.message || err.response?.data || err;
                console.error(`Error fetching movie details for ID ${movieId}:`, errorMessage);
                setError('Не удалось загрузить информацию о фильме.');
                setLoading(false);
            });

        fetchShowtimesForMovie(movieId);
        fetchReviewsForMovie(movieId);

    }, [movieId]);

    // <-- Эффект для загрузки билетов при выборе сеанса -->
    useEffect(() => {
        if (selectedShowtime) {
            fetchTicketsForShowtime(selectedShowtime.id);
            setSelectedSeats([]);
            setTotalPrice(0);
        } else {
            setShowtimeTickets([]);
            setTicketsError(null);
            setTicketsLoading(false);
            setSelectedSeats([]);
            setTotalPrice(0);
        }
    }, [selectedShowtime]);

    // <-- Эффект для пересчета общей цены при изменении выбранных мест -->
    useEffect(() => {
        const calculatedPrice = selectedSeats.length * SEAT_PRICE;
        setTotalPrice(calculatedPrice);
    }, [selectedSeats]);


    // --- ЛОГИКА УПРАВЛЕНИЯ МОДАЛЬНЫМИ ОКНАМИ И ФОРМАМИ ---

    // Сеансы
    const handleOpenShowtimeModal = (showtime?: Showtime) => {
        if (!currentUser /* TODO: Проверка роли администратора */) {
            // alert("У вас нет прав на добавление/редактирование сеансов.");
            // return;
        }

        if (showtime) {
            setShowtimeFormData({
                id: showtime.id,
                movieId: movieId || 0,
                dateTime: showtime.dateTime,
                type: showtime.type || '',
            });
        } else {
            setShowtimeFormData({
                movieId: movieId || 0,
                dateTime: '',
                type: '',
            });
        }
        setIsShowtimeModalOpen(true);
    };

    const handleCloseShowtimeModal = () => {
        setIsShowtimeModalOpen(false);
        setShowtimeFormData({ movieId: movieId || 0, dateTime: '', type: '' });
    };

    // === ИСПРАВЛЕНО: Обновлена сигнатура для поддержки SelectChangeEvent ===
    const handleShowtimeInputChange = (
        event: SelectChangeEvent<string> | React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>, // Объединяем типы событий от Select и TextField
    ) => {
        const { name, value } = event.target;
        // Обработка как из TextField, так и из Select (SelectChangeEvent тоже имеет target.name и target.value)
        // Type assertion to tell TypeScript that name is a key of ShowtimeFormData
        setShowtimeFormData(prevState => ({ ...prevState, [name as keyof ShowtimeFormData]: value as any }));
    };
    // ======================================================================

    // Хендлер для выбора сеанса
    const handleSelectShowtime = (showtime: Showtime) => {
        if (selectedShowtime && selectedShowtime.id === showtime.id) {
            setSelectedShowtime(null);
        } else {
            setSelectedShowtime(showtime);
        }
    };


    const handleSaveShowtime = async () => {
        if (!showtimeFormData.dateTime.trim() || !showtimeFormData.type.trim()) {
            alert("Пожалуйста, заполните Время начала и Тип сеанса.");
            return;
        }

        setIsSubmittingShowtime(true);

        const isEditing = showtimeFormData.id !== undefined;

        const showtimeDataToSend = {
            id: showtimeFormData.id,
            movieId: showtimeFormData.movieId,
            dateTime: showtimeFormData.dateTime,
            type: showtimeFormData.type,
            hall: '1',
            theaterId: 1,
        };

        if (!isEditing) {
            delete showtimeDataToSend.id;
        }

        const apiCall = isEditing
            ? axios.put<Showtime>(`${SHOWTIMES_API_URL}/${showtimeFormData.id}`, showtimeDataToSend)
            : axios.post<Showtime>(SHOWTIMES_API_URL, showtimeDataToSend);

        try {
            await apiCall;
            if (movieId !== null) {
                fetchShowtimesForMovie(movieId);
            }
        } catch (err: any) {
            console.error(`Ошибка при ${isEditing ? 'редактировании' : 'добавлении'} сеанса:`, err.response?.data || err.message);
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || err;
            alert(`Не удалось ${isEditing ? 'отредактировать' : 'добавить'} сеанс. ` + (typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage)));
        } finally {
            setIsSubmittingShowtime(false);
            handleCloseShowtimeModal();
        }
    };

    const handleDeleteShowtime = async (id: number) => {
        if (window.confirm(`Выверены, что хотите удалить сеанс?`)) {
            try {
                await axios.delete(`${SHOWTIMES_API_URL}/${id}`);
                setShowtimes(showtimes.filter(st => st.id !== id));
                console.log(`Сеанс с ID ${id} успешно удален.`);
                if (selectedShowtime && selectedShowtime.id === id) {
                    setSelectedShowtime(null);
                }
                if (movieId !== null) {
                    fetchShowtimesForMovie(movieId);
                }

            } catch (err: any) {
                console.error(`Ошибка при удалении сеанса с ID ${id}:`, err.response?.data || err.message);
                const errorMessage = err.response?.data?.message || err.response?.data || err.message || err;
                alert('Не удалось удалить сеанс. ' + (typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage)));
            }
        }
    };


    // Отзывы
    const handleOpenReviewModal = (review?: Review) => {
        if (!currentUser && review === undefined) {
            alert("Войдите, чтобы оставить отзыв.");
            return;
        }
        if (!movie) {
            console.error("Cannot open review modal: movie details not loaded.");
            alert("Не удалось загрузить данные фильма.");
            return;
        }

        if (review) {
            setReviewFormData({
                id: review.id,
                rating: review.rating,
                content: review.content || '',
                userId: review.user?.id ?? currentUser?.id ?? 0,
                movieId: review.movie?.id ?? movie.id ?? 0,
            });
        } else {
            if (!currentUser?.id || !movie?.id) {
                console.error("Cannot open review modal for new review: currentUser or movie missing ID.", { currentUser, movie });
                alert("Не удалось определить пользователя или фильм для отзыва.");
                return;
            }
            setReviewFormData({
                movieId: movie.id,
                userId: currentUser.id,
                rating: 0,
                content: '',
            });
        }
        setIsReviewModalOpen(true);
    };

    const handleCloseReviewModal = () => {
        setIsReviewModalOpen(false);
        setReviewFormData({ movieId: movieId || 0, rating: 0, content: '' });
    };

    const handleReviewInputChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        if (name === 'rating') {
            const parsedValue = parseInt(value, 10);
            const ratingValue = isNaN(parsedValue) ? 0 : Math.max(0, Math.min(10, parsedValue));
            setReviewFormData(prevState => ({ ...prevState, [name]: ratingValue }));
        } else {
            setReviewFormData(prevState => ({ ...prevState, [name]: value }));
        }
    };

    const handleRatingChange = (_event: React.ChangeEvent<{}>, newValue: number | null) => {
        setReviewFormData(prevState => ({ ...prevState, rating: newValue || 0 }));
    };

    const handleSaveReview = async () => {
        if (!currentUser || !currentUser.id || typeof currentUser.id !== 'number') {
            console.error("Current user is not logged in or user ID is invalid:", currentUser);
            alert("Пользователь не авторизован.");
            return;
        }

        if (reviewFormData.rating < 1 || reviewFormData.rating > 10 || !reviewFormData.content.trim()) {
            alert("Пожалуйста, установите рейтинг (от 1 до 10) и напишите комментарий.");
            return;
        }

        if (!reviewFormData.movieId || !reviewFormData.userId) {
            console.error("reviewFormData missing movieId or userId:", reviewFormData);
            alert("Не удалось связать отзыв с фильмом или пользователем.");
            return;
        }


        setIsSubmittingReview(true);

        const isEditing = reviewFormData.id !== undefined;

        const reviewDataToSend = {
            id: reviewFormData.id,
            movieId: reviewFormData.movieId,
            userId: reviewFormData.userId,
            rating: reviewFormData.rating,
            content: reviewFormData.content.trim(),
        };


        const apiCall = isEditing
            ? axios.put<Review>(`${REVIEWS_API_URL}/${reviewFormData.id}`, reviewDataToSend)
            : axios.post<Review>(REVIEWS_API_URL, reviewDataToSend);

        try {
            const response = await apiCall;
            const savedReview = response.data;

            const reviewWithUser: Review = {
                id: savedReview.id,
                rating: savedReview.rating,
                content: savedReview.content,
                user: currentUser ? { id: currentUser.id, username: currentUser.username } : { id: reviewDataToSend.userId, username: 'Аноним' },
                movie: movie ? { id: movie.id } : null,
            };

            if (isEditing) {
                setReviews(reviews.map(rev => rev.id === reviewWithUser.id ? reviewWithUser : rev));
            } else {
                setReviews([...reviews, reviewWithUser]);
            }

            if (movie) {
                fetchReviewsForMovie(movie.id);
            }

            handleCloseReviewModal();

        } catch (err: any) {
            console.error(`Ошибка при ${isEditing ? 'редактировании' : 'добавлении'} отзыва:`, err.response?.data || err.message);
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || err;
            alert(`Не удалось ${isEditing ? 'отредактировать' : 'добавить'} отзыв. ` + (typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage)));
        } finally {
            setIsSubmittingReview(false);
        }
    };

    const handleDeleteReview = async (id: number) => {
        if (window.confirm(`Выверены, что хотите удалить отзыв?`)) {
            try {
                await axios.delete(`${REVIEWS_API_URL}/${id}`);
                setReviews(reviews.filter(rev => rev.id !== id));
                if (movieId !== null) {
                    fetchReviewsForMovie(movieId);
                }

            } catch (err: any) {
                console.error(`Ошибка при удалении отзыва с ID ${id}:`, err.response?.data || err.message);
                const errorMessage = err.response?.data?.message || err.response?.data || err.message || err;
                alert('Не удалось удалить отзыв. ' + (typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage)));
            }
        }
    };


    // <-- ЛОГИКА УПРАВЛЕНИЯ МОДАЛКОЙ И СОХРАНЕНИЕМ ФИЛЬМА -->
    // TODO: Добавить логику открытия модалки редактирования фильма
    // const handleOpenMovieModal = (movie?: Movie) => { ... }


    const handleCloseMovieModal = () => {
        setIsMovieModalOpen(false);
        setDialogFormData({ id: undefined, title: '', director: '', releaseYear: '', genre: '' });
    };

    // Хендлер для изменения полей формы фильма
    const handleDialogInputChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        setDialogFormData(prevState => ({ ...prevState, [name]: value }));
    };


    // Функция сохранения фильма
    const handleSaveDialogForm = async () => {
        if (
            !dialogFormData.title.trim()
            || !dialogFormData.director.trim()
            || !dialogFormData.releaseYear.trim()
            || !dialogFormData.genre.trim()
        ) {
            alert('Пожалуйста, заполните все поля фильма.');
            return;
        }

        const parsedYear = parseInt(dialogFormData.releaseYear, 10);
        if (isNaN(parsedYear)) {
            alert('Пожалуйста, введите корректный год выхода.');
            return;
        }

        const movieDataToSend = {
            id: dialogFormData.id,
            title: dialogFormData.title.trim(),
            director: dialogFormData.director.trim(),
            releaseYear: parsedYear,
            genre: dialogFormData.genre.trim(),
        };

        const isEditing = dialogFormData.id !== undefined;

        const apiCall = isEditing
            ? axios.put<Movie>(`${MOVIES_API_URL}/${dialogFormData.id}`, movieDataToSend)
            : axios.post<Movie>(MOVIES_API_URL, movieDataToSend);

        setIsSubmittingMovie(true);

        try {
            const response = await apiCall;
            const updatedMovie = response.data;

            setMovie(updatedMovie);

            if (updatedMovie.id !== undefined && updatedMovie.id !== null) {
                fetchReviewsForMovie(updatedMovie.id);
            }

            handleCloseMovieModal();

        } catch (err: any) {
            console.error(`[handleSaveMovie] Ошибка при ${isEditing ? 'обновлении' : 'добавлении'} фильма:`, err.response?.data || err.message);
            const errorMessage = err.response?.data?.message || err.response?.data || err.message || err;
            alert(
                `Не удалось ${isEditing ? 'обновить' : 'добавить'} фильм. `
                + (typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage))
            );
        } finally {
            setIsSubmittingMovie(false);
        }
    };
    // <-- КОНЕЦ ЛОГИКИ УПРАВЛЕНИЯ МОДАЛКОЙ И СОХРАНЕНИЕМ ФИЛЬМА -->

    // <-- Хендлер для кнопки "Купить билет" -->
    const handleBuyTicket = () => {
        if (selectedShowtime) {
            setIsTicketModalOpen(true);
        } else {
            alert("Пожалуйста, выберите сеанс.");
        }
    };
    // <-- КОНЕЦ ДОБАВЛЕНО -->

    // <-- Хендлер для закрытия модалки билетов -->
    const handleCloseTicketModal = () => {
        setIsTicketModalOpen(false);
        setShowtimeTickets([]);
        setTicketsError(null);
        setTicketsLoading(false);
        setSelectedSeats([]);
        setTotalPrice(0);
        setIsPurchasing(false);
        setSelectedShowtime(null);
    };
    // <-- КОНЕЦ Хендлера -->

    const handleSelectSeat = (seatIdentifier: string) => {
        const isOccupied = showtimeTickets.some(ticket => ticket.seatNumber === seatIdentifier);

        if (isOccupied) {
            // TODO: Можно добавить визуальное уведомление пользователю
            return;
        }

        setSelectedSeats(prevSelectedSeats => {
            if (prevSelectedSeats.includes(seatIdentifier)) {
                return prevSelectedSeats.filter(seat => seat !== seatIdentifier);
            } else {
                return [...prevSelectedSeats, seatIdentifier];
            }
        });
    };


    // --- РЕНДЕРИНГ КОМПОНЕНТА ---

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
                <Typography variant="h6" sx={{ ml: 2 }}>Загрузка деталей фильма...</Typography>
            </Box>
        );
    }

    if (error) {
        return <Typography color="error" sx={{ mt: 4, textAlign: 'center' }}>{error}</Typography>;
    }

    if (!movie) {
        return <Typography color="textSecondary" sx={{ mt: 4, textAlign: 'center' }}>Фильм не найден.</Typography>;
    }

    const averageRating = calculateAverageRating(reviews);
    const hasNumericRating = typeof averageRating === 'number';
    const ratingColor = hasNumericRating ? getRatingColor(averageRating!) : '#ffffff';


    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            {/* Информация о фильме */}
            <Paper elevation={3} sx={{ p: 3, mb: 4, bgcolor: '#212121', color: '#ffffff', width: 270 }}> {/* Убран width */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h4" gutterBottom>{movie.title}</Typography>
                </Box>
                <Typography variant="h6" color="textSecondary" sx={{ mb: 2, color: '#bdbdbd' }}>{movie.director}</Typography>
                <Typography variant="body1" sx={{ mb: 1 }}>Год выхода: {movie.releaseYear}</Typography>
                <Typography variant="body1" sx={{ mb: 1 }}>Жанр: {movie.genre}</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Typography variant="body1" sx={{ mr: 1 }}>Рейтинг:</Typography>
                    {hasNumericRating ? (
                        <Typography variant="h6" component="span" sx={{ color: ratingColor, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                            <StarIcon sx={{ fontSize: 'large', verticalAlign: 'middle', mr: 0.5, color: ratingColor }} />
                            {averageRating!.toFixed(1)}
                        </Typography>
                    ) : (
                        <Typography variant="h6" component="span" sx={{ color: '#ffffff' }}>
                            Нет оценок
                        </Typography>
                    )}
                    <Typography variant="body2" sx={{ ml: 1, color: '#bdbdbd' }}>({reviews.length})</Typography>
                </Box>
            </Paper>

            <Divider sx={{ bgcolor: '#616161', mb: 4 }} />

            {/* Секция Сеансы */}
            <Box sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h5" sx={{ color: '#ffffff' }}>Расписание сеансов</Typography>
                        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenShowtimeModal()}>
                            Добавить сеанс
                        </Button>
                </Box>
                {showtimesLoading && <Box sx={{ display: 'flex', justifyContent: 'center' }}><CircularProgress size={24} /></Box>}
                {showtimesError && <Typography color="error">{showtimesError}</Typography>}
                {!showtimesLoading && !showtimesError && showtimes.length === 0 && (
                    <Typography color="textSecondary" sx={{ color: '#bdbdbd' }}>Сеансы для этого фильма пока не добавлены.</Typography>
                )}
                {!showtimesLoading && !showtimesError && showtimes.length > 0 && (
                    // Контейнер для горизонтального отображения сеансов
                    <Box sx={{ display: 'flex', flexDirection: 'row', gap: 2, overflowX: 'auto', pb: 1 }}>
                        {showtimes.map(showtime => (
                            // Элемент сеанса - теперь кликабельный Paper
                            <Paper
                                key={showtime.id} // Ключ для элемента списка
                                elevation={selectedShowtime && selectedShowtime.id === showtime.id ? 8 : 2}
                                onClick={() => handleSelectShowtime(showtime)} // Обработчик клика для выбора
                                sx={{
                                    p: 2,
                                    minWidth: 150,
                                    maxWidth: 200, // Ограничиваем максимальную ширину
                                    flexShrink: 0,
                                    textAlign: 'center',
                                    cursor: 'pointer',
                                    bgcolor: selectedShowtime && selectedShowtime.id === showtime.id ? '#303030' : '#424242',
                                    color: '#ffffff',
                                    border: selectedShowtime && selectedShowtime.id === showtime.id ? '2px solid' : 'none',
                                    borderColor: 'primary.main',
                                    transition: 'all 0.2s ease-in-out',
                                    '&:hover': {
                                        bgcolor: selectedShowtime && selectedShowtime.id === showtime.id ? '#303030' : '#505050',
                                    },
                                }}
                            >
                                <Typography variant="subtitle1" sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ffffff', mb: 0.5, mr: 2.5 }}>
                                    <AccessTimeIcon sx={{  color: '#bdbdbd' }} />
                                    {showtime.dateTime ? new Date(showtime.dateTime).toLocaleString() : 'Время неизвестно'}
                                </Typography>

                                {showtime.type && (
                                    <Typography variant="body2" color="textSecondary" sx={{ color: '#bdbdbd', alignItems: 'center', ml: 0.7}}>
                                        Тип: {showtime.type} {/* Тип */}
                                    </Typography>
                                )}
                                {/* Кнопки редактирования/удаления сеансов */}
                                    <Box sx={{ mt: 1 }}>
                                        <IconButton size="small" aria-label="edit showtime" onClick={(e) => { e.stopPropagation(); handleOpenShowtimeModal(showtime); }}>
                                            <EditIcon fontSize="small" color="primary" />
                                        </IconButton>
                                        <IconButton size="small" aria-label="delete showtime" onClick={(e) => { e.stopPropagation(); handleDeleteShowtime(showtime.id); }}>
                                            <DeleteIcon fontSize="small" color="error" />
                                        </IconButton>
                                    </Box>
                            </Paper>
                        ))}
                    </Box>
                )}

                {/* Кнопка "Купить билет" */}
                <Box sx={{ mt: 3, textAlign: 'center' }}>
                    <Button
                        variant="contained"
                        color="primary"
                        size="large"
                        disabled={selectedShowtime === null}
                        onClick={handleBuyTicket}
                    >
                        Купить билет
                    </Button>
                    {selectedShowtime && (
                        <Typography variant="body2" color="textSecondary" sx={{ mt: 1, color: '#bdbdbd' }}>
                            Выбран: {selectedShowtime.dateTime ? new Date(selectedShowtime.dateTime).toLocaleString() : 'Сеанс неизвестен'}
                        </Typography>
                    )}
                </Box>

            </Box>


            <Divider sx={{ bgcolor: '#616161', mb: 4 }} />

            {/* Секция Отзывы */}
            <Box sx={{ mb: 6, pb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h5" sx={{ color: '#ffffff' }}>Отзывы</Typography>
                    {currentUser ? (
                        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenReviewModal()}>
                            Оставить отзыв
                        </Button>
                    ) : (
                        <Typography color="textSecondary" sx={{ color: '#bdbdbd' }}>Войдите, чтобы оставить отзыв.</Typography>
                    )}
                </Box>
                {reviewsLoading && <Box sx={{ display: 'flex', justifyContent: 'center' }}><CircularProgress size={24} /></Box>}
                {reviewsError && <Typography color="error">{reviewsError}</Typography>}
                {!reviewsLoading && !reviewsError && reviews.length === 0 && (
                    <Typography color="textSecondary" sx={{ color: '#bdbdbd' }}>Отзывов для этого фильма пока нет. Будьте первым!</Typography>
                )}
                {!reviewsLoading && !reviewsError && reviews.length > 0 && (
                    <List sx={{
                        bgcolor: '#212121',
                        color: '#ffffff',
                        pb: 2
                    }}>
                        {reviews.map(review => (
                            <ListItem
                                key={review.id}
                                secondaryAction={
                                    <Box>
                                        {currentUser && review.user?.id === currentUser.id && (
                                            <>
                                                <IconButton  aria-label="edit review" onClick={() => handleOpenReviewModal(review)}>
                                                    <EditIcon fontSize="small" color="primary" />
                                                </IconButton>
                                                <IconButton edge="end" aria-label="delete review" onClick={() => handleDeleteReview(review.id)}>
                                                    <DeleteIcon fontSize="small" color="error" />
                                                </IconButton>
                                            </>
                                        )}
                                    </Box>
                                }
                                sx={{ borderBottom: '1px solid #616161', '&:last-child': { borderBottom: 'none' } }}
                            >
                                <ListItemText
                                    primary={<>
                                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                            <Typography variant="subtitle1" component="span" sx={{ mr: 1, fontWeight: 'bold', color: '#ffffff' }}>
                                                {review.user?.username || 'Аноним'}
                                            </Typography>
                                            {typeof review.rating === 'number' && review.rating >= 0 && review.rating <= 10 ? (
                                                <Rating
                                                    name={`review-rating-${review.id}`}
                                                    value={review.rating}
                                                    readOnly
                                                    max={10}
                                                    precision={1}
                                                    icon={<StarIcon fontSize="inherit" style={{ color: getRatingColor(review.rating) }} />}
                                                    emptyIcon={<StarIcon fontSize="inherit" style={{ color: '#bdbdbd' }} />}
                                                />
                                            ) : (
                                                <Typography variant="body2" component="span" sx={{ color: '#bdbdbd' }}>Нет рейтинга</Typography>
                                            )}
                                        </Box>
                                    </>}
                                    secondary={review.content}
                                    primaryTypographyProps={{ color: '#ffffff' }}
                                    secondaryTypographyProps={{ color: '#bdbdbd' }}
                                />
                            </ListItem>
                        ))}
                    </List>
                )}
            </Box>


            {/* --- МОДАЛЬНЫЕ ОКНА --- */}
            {/* Модальное окно для добавления/редактирования сеанса */}
            <Dialog open={isShowtimeModalOpen} onClose={handleCloseShowtimeModal}>
                <DialogTitle>{showtimeFormData.id !== undefined ? 'Редактировать сеанс' : 'Добавить новый сеанс'}</DialogTitle>
                <DialogContent>
                    <Box
                        component="form"
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                        noValidate
                        autoComplete="off"
                    >
                        {/* Поле Время начала */}
                        <TextField
                            autoFocus
                            margin="dense"
                            name="dateTime"
                            label="Время начала"
                            type="datetime-local"
                            fullWidth
                            variant="outlined"
                            value={showtimeFormData.dateTime}
                            onChange={handleShowtimeInputChange}
                            required
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />

                        <FormControl fullWidth margin="dense" variant="outlined" required>
                            <InputLabel id="showtime-type-label">Тип сеанса</InputLabel>
                            <Select
                                labelId="showtime-type-label"
                                id="showtime-type"
                                name="type"
                                value={showtimeFormData.type}
                                onChange={handleShowtimeInputChange}
                                label="Тип сеанса"
                            >
                                <MenuItem value="2D">2D</MenuItem>
                                <MenuItem value="3D">3D</MenuItem>
                            </Select>
                        </FormControl>

                    </Box>
                </DialogContent>
                <DialogActions sx={{ justifyContent: 'space-between', px: 3, py: 2 }}>
                    <Button onClick={handleCloseShowtimeModal} color="secondary" disabled={isSubmittingShowtime}>
                        Отмена
                    </Button>
                    <Button onClick={handleSaveShowtime} color="primary" variant="contained" disabled={isSubmittingShowtime || !showtimeFormData.dateTime.trim() || !showtimeFormData.type.trim()}>
                        {isSubmittingShowtime ? <CircularProgress size={24} color="inherit" /> : (showtimeFormData.id !== undefined ? 'Сохранить' : 'Добавить')}
                    </Button>
                </DialogActions>
            </Dialog>


            {/* Модальное окно для добавления/редактирования отзыва */}
            <Dialog open={isReviewModalOpen} onClose={handleCloseReviewModal}>
                <DialogTitle>{reviewFormData.id !== undefined ? 'Редактировать отзыв' : 'Оставить отзыв'}</DialogTitle>
                <DialogContent>
                    <Box
                        component="form"
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                        noValidate
                        autoComplete="off"
                    >
                        <Typography component="legend" sx={{ color: '#bdbdbd' }}>Рейтинг (от 1 до 10)</Typography>
                        <Rating
                            name="rating"
                            value={reviewFormData.rating}
                            max={10}
                            precision={1}
                            onChange={handleRatingChange}
                        />
                        <TextField
                            margin="dense"
                            name="content"
                            label="Комментарий"
                            type="text"
                            fullWidth
                            multiline
                            rows={4}
                            variant="outlined"
                            value={reviewFormData.content}
                            onChange={handleReviewInputChange}
                            required
                        />
                    </Box>
                </DialogContent>
                <DialogActions sx={{ justifyContent: 'space-between', px: 3, py: 2 }}>
                    <Button onClick={handleCloseReviewModal} color="secondary" disabled={isSubmittingReview}>
                        Отмена
                    </Button>
                    <Button onClick={handleSaveReview} color="primary" variant="contained" disabled={isSubmittingReview || reviewFormData.rating < 1 || reviewFormData.rating > 10 || !reviewFormData.content.trim()}>
                        {isSubmittingReview ? <CircularProgress size={24} color="inherit" /> : (reviewFormData.id !== undefined ? 'Сохранить' : 'Оставить')}
                    </Button>
                </DialogActions>
            </Dialog>


            {/* Модальное окно для редактирования фильма */}
            <Dialog open={isMovieModalOpen} onClose={handleCloseMovieModal}>
                <DialogTitle>{dialogFormData.id !== undefined ? 'Редактировать фильм' : 'Добавить новый фильм'}</DialogTitle>
                <DialogContent>
                    <Box
                        component="form"
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                        noValidate
                        autoComplete="off"
                    >
                        <TextField
                            autoFocus
                            margin="dense"
                            name="title"
                            label="Название"
                            type="text"
                            fullWidth
                            variant="outlined"
                            value={dialogFormData.title}
                            onChange={handleDialogInputChange}
                            required
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
                            required
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
                            inputProps={{ min: 1800, max: new Date().getFullYear() + 5 }}
                            required
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
                            required
                        />
                    </Box>
                </DialogContent>
                <DialogActions sx={{ justifyContent: 'space-between', px: 3, py: 2 }}>
                    <Button onClick={handleCloseMovieModal} color="secondary" disabled={isSubmittingMovie}>
                        Отмена
                    </Button>
                    <Button onClick={handleSaveDialogForm} color="primary" variant="contained" disabled={isSubmittingMovie || !dialogFormData.title.trim() || !dialogFormData.director.trim() || !dialogFormData.releaseYear.trim() || !dialogFormData.genre.trim() || isNaN(parseInt(dialogFormData.releaseYear, 10))}>
                        {isSubmittingMovie ? <CircularProgress size={24} color="inherit" /> : (dialogFormData.id !== undefined ? 'Сохранить' : 'Добавить')}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* <-- ДОБАВЛЕНО: Модальное окно для выбора места (Билетов) --> */}
            <Dialog open={isTicketModalOpen} onClose={handleCloseTicketModal} maxWidth="lg" fullWidth>
                <DialogTitle>
                    Выберите места для сеанса
                    {selectedShowtime ? (
                        <Typography
                            variant="subtitle1"
                            color="textSecondary"
                            component="span"
                            sx={{ ml: 1 }}
                        >
                            {selectedShowtime.dateTime ? new Date(selectedShowtime.dateTime).toLocaleString() : 'Сеанс неизвестен'}
                        </Typography>
                    ) : ''}
                </DialogTitle>
                <DialogContent>
                    {/* Экран кинозала */}
                    <Box sx={{ bgcolor: '#424242', color: '#ffffff', p: 1, mb: 3, textAlign: 'center', fontWeight: 'bold' }}>
                        ЭКРАН
                    </Box>

                    {/* Отображение состояния загрузки/ошибки билетов */}
                    {ticketsLoading && <Box sx={{ display: 'flex', justifyContent: 'center' }}><CircularProgress size={24} /></Box>}
                    {ticketsError && <Typography color="error">{ticketsError}</Typography>}

                    {!ticketsLoading && !ticketsError && selectedShowtime && (
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
                            {/* Генерация рядов и мест */}
                            {[...Array(ROWS)].map((_, rowIndex) => (
                                <Box key={`row-${rowIndex}`} sx={{ display: 'flex', justifyContent: 'center', gap: 1, alignItems: 'center' }}>
                                    {/* Номер ряда слева */}
                                    <Typography variant="caption" sx={{ width: 30, textAlign: 'right', mr: 1, fontWeight: 'bold' }}>Ряд {rowIndex + 1}</Typography>
                                    {[...Array(SEATS_PER_ROW)].map((_, seatIndex) => {
                                        const seatIdentifier = `${rowIndex + 1}-${seatIndex + 1}`;

                                        const isOccupied = showtimeTickets.some(ticket => ticket.seatNumber === seatIdentifier);
                                        const isSelected = selectedSeats.includes(seatIdentifier);

                                        const seatColor = isOccupied ? '#757575' : (isSelected ? '#1976d2' : '#616161');
                                        const textColor = isOccupied ? '#e0e0e0' : (isSelected ? '#ffffff' : '#ffffff');


                                        return (
                                            <Box
                                                key={seatIdentifier}
                                                sx={{
                                                    width: 35,
                                                    height: 35,
                                                    bgcolor: seatColor,
                                                    display: 'flex',
                                                    justifyContent: 'center',
                                                    alignItems: 'center',
                                                    color: textColor,
                                                    borderRadius: 1,
                                                    fontSize: '0.8rem',
                                                    fontWeight: 'bold',
                                                    cursor: isOccupied ? 'not-allowed' : 'pointer',
                                                    opacity: isOccupied ? 0.7 : 1,
                                                    border: isSelected ? '2px solid white' : 'none',
                                                    transition: 'all 0.1s ease-in-out',
                                                    '&:hover': {
                                                        transform: isOccupied ? 'none' : 'scale(1.1)',
                                                    }
                                                }}
                                                onClick={() => handleSelectSeat(seatIdentifier)}
                                            >
                                                {seatIndex + 1}
                                            </Box>
                                        );
                                    })}
                                    {/* Номер ряда справа (опционально, для удобства) */}
                                    <Typography variant="caption" sx={{ width: 30, textAlign: 'left', ml: 1, fontWeight: 'bold' }}>{rowIndex + 1}</Typography>
                                </Box>
                            ))}
                        </Box>
                    )}

                    {/* Индикаторы занятости и выбора */}
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3, mt: 3 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <Box sx={{ width: 20, height: 20, bgcolor: '#616161', mr: 1, borderRadius: 1 }}></Box>
                            <Typography variant="body2" sx={{ color: '#ffffff' }}>Свободно</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <Box sx={{ width: 20, height: 20, bgcolor: '#757575', mr: 1, borderRadius: 1 }}></Box>
                            <Typography variant="body2" sx={{ color: '#ffffff' }}>Занято</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <Box sx={{ width: 20, height: 20, bgcolor: '#1976d2', mr: 1, borderRadius: 1 }}></Box>
                            <Typography variant="body2" sx={{ color: '#ffffff' }}>Выбрано</Typography>
                        </Box>
                    </Box>

                </DialogContent>
                <DialogActions sx={{ justifyContent: 'space-between', px: 3, py: 2 }}>
                    {/* Отображение общей цены */}
                    <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
                        Итого: {totalPrice} ₽
                    </Typography>

                    {/* Кнопки Отмена и Купить */}
                    <Box>
                        <Button onClick={handleCloseTicketModal} color="secondary">Отмена</Button>
                        <Button
                            onClick={handlePurchaseTickets}
                            color="primary"
                            variant="contained"
                            disabled={selectedSeats.length === 0 || isPurchasing || !currentUser}
                            sx={{ ml: 2 }}
                        >
                            {isPurchasing ? <CircularProgress size={24} color="inherit" /> : 'Купить'}
                        </Button>
                    </Box>
                </DialogActions>
            </Dialog>
            {/* <-- КОНЕЦ ДОБАВЛЕНО --> */}


        </Container>
    );
}


export default MovieDetailsPage;

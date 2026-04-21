import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    CircularProgress,
    IconButton,
    Alert,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { apiGet, apiPut, apiDelete } from '../api';

import type { User, AuthUser } from '../types';

// Тип для отзыва
interface Review {
    id: number;
    rating: number;
    content: string;
    user: User;
}

// Тип для фильма
interface Movie {
    id: number;
    title: string;
    director: string;
    releaseYear: number;
    genre: string;
    reviews: Review[];
}

// Тип для сеанса
interface Showtime {
    id: number;
    dateTime: string;
    type: string;
    movie: Movie;
}

// Тип для места
interface Seat {
    id: number;
    seatRow: number;
    number: number;
    available: boolean;
}

// Основной тип для билета
interface Ticket {
    id: number;
    seatNumber: string;
    price: number;
    user: User;
    seat: Seat;
    showtime: Showtime;
}

// Пропсы компонента
interface MyTicketsPageProps {
    currentUser: AuthUser;
}

const TICKETS_API_URL = 'http://localhost:8081/api/tickets';
const USER_TICKETS_API_URL = `${TICKETS_API_URL}/my`;

function MyTicketsPage({ currentUser }: MyTicketsPageProps) {
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [openDialog, setOpenDialog] = useState(false);
    const [dialogType, setDialogType] = useState<'edit' | 'delete' | null>(null);
    const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
    const [editFormData, setEditFormData] = useState<Partial<Ticket>>({});

    const fetchTickets = async () => {
        setLoading(true);
        setError(null);
        try {
            const timestamp = Date.now();
            const url = `${USER_TICKETS_API_URL}?_t=${timestamp}`;

            console.log(`[fetchTickets] GET: ${url}`);

            const response = await apiGet<Ticket[]>(url);

            if (response.status >= 200 && response.status < 300) {
                setTickets(response.data || []);
            } else {
                setError(`Ошибка при загрузке билетов: Статус ${response.status}`);
            }
        } catch (err: any) {
            console.error("Ошибка при загрузке билетов:", err.response?.data || err.message || err);
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка";
            setError(`Не удалось загрузить билеты: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (currentUser) {
            fetchTickets();
        } else {
            setTickets([]);
            setLoading(false);
        }
    }, [currentUser]);

    const handleDeleteTicket = (ticket: Ticket) => {
        setSelectedTicket(ticket);
        setDialogType('delete');
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setDialogType(null);
        setSelectedTicket(null);
        setEditFormData({});
        setError(null);
    };

    const handleEditFormChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        setEditFormData((prevState: Partial<Ticket>) => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleConfirmEdit = async () => {
        if (!selectedTicket || !currentUser) {
            setError("Нет выбранного билета или пользователя.");
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const updateUrl = `${TICKETS_API_URL}/${selectedTicket.id}`;

            const dataToSend = {
                price: editFormData.price,
            };

            const response = await apiPut(updateUrl, dataToSend);

            if (response.status >= 200 && response.status < 300) {
                console.log("Билет обновлен:", response.data);
                fetchTickets();
                handleCloseDialog();
            } else {
                setError(`Ошибка при обновлении: Статус ${response.status}`);
            }

        } catch (err: any) {
            console.error("Ошибка при обновлении билета:", err);
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка";
            setError(`Не удалось обновить билет: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };

    const handleConfirmDelete = async () => {
        if (!selectedTicket || !currentUser) {
            setError("Нет выбранного билета или пользователя.");
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const deleteUrl = `${TICKETS_API_URL}/${selectedTicket.id}`;
            const response = await apiDelete(deleteUrl);

            if (response.status === 200 || response.status === 204) {
                console.log("Билет удален:", selectedTicket.id);
                fetchTickets();
                handleCloseDialog();
            } else {
                setError(`Ошибка при удалении: Статус ${response.status}`);
            }

        } catch (err: any) {
            console.error("Ошибка при удалении билета:", err);
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка";
            setError(`Не удалось удалить билет: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom>
                Мои билеты
            </Typography>

            {error && !openDialog && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}

            {loading && !openDialog ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <CircularProgress />
                </Box>
            ) : (
                tickets.length > 0 ? (
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} aria-label="my tickets table">
                            <TableHead>
                                <TableRow>
                                    <TableCell align="center">Фильм</TableCell>
                                    <TableCell align="center">Дата</TableCell>
                                    <TableCell align="center">Время</TableCell>
                                    <TableCell align="center">Место(а)</TableCell>
                                    <TableCell align="center">Цена</TableCell>
                                    <TableCell align="center">Действия</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {tickets.map((ticket) => (
                                    <TableRow key={ticket.id}>
                                        <TableCell align="center">
                                            {ticket.showtime?.movie?.title || 'Название неизвестно'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.showtime?.dateTime
                                                ? new Date(ticket.showtime.dateTime).toLocaleDateString()
                                                : 'Дата неизвестна'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.showtime?.dateTime
                                                ? new Date(ticket.showtime.dateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                                                : 'Время неизвестно'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.seat
                                                ? `Ряд ${ticket.seat.seatRow}, Место ${ticket.seat.number}`
                                                : 'Место неизвестно'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.price?.toFixed(2) || '0.00'} р.
                                        </TableCell>
                                        <TableCell align="center">
                                            <IconButton
                                                aria-label="delete ticket"
                                                onClick={() => handleDeleteTicket(ticket)}
                                                color="error"
                                                size="small"
                                                sx={{ ml: 1 }}
                                                disabled={loading}
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                ) : (
                    !loading && !error && (
                        <Typography variant="body1" align="center" sx={{ mt: 4 }}>
                            У вас пока нет купленных билетов.
                        </Typography>
                    )
                )
            )}

            {/* Диалог */}
            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>
                    {dialogType === 'edit' ? 'Редактировать билет' : 'Удалить билет'}
                </DialogTitle>

                {error && openDialog && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                <DialogContent>
                    {dialogType === 'edit' && selectedTicket && (
                        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <TextField
                                margin="dense"
                                label="Фильм"
                                fullWidth
                                value={selectedTicket.showtime?.movie?.title || ''}
                                disabled
                            />
                            <TextField
                                margin="dense"
                                label="Дата/Время сеанса"
                                fullWidth
                                value={selectedTicket.showtime?.dateTime || ''}
                                disabled
                            />
                            <TextField
                                margin="dense"
                                label="Место(а)"
                                fullWidth
                                value={selectedTicket.seat
                                    ? `Ряд ${selectedTicket.seat.seatRow}, Место ${selectedTicket.seat.number}`
                                    : ''}
                                disabled
                            />
                            <TextField
                                margin="dense"
                                name="price"
                                label="Цена"
                                type="number"
                                fullWidth
                                value={editFormData.price ?? ''}
                                onChange={handleEditFormChange}
                            />
                        </Box>
                    )}

                    {dialogType === 'delete' && selectedTicket && (
                        <Typography variant="body1">
                            Вы уверены, что хотите удалить билет на фильм "
                            {selectedTicket.showtime?.movie?.title || 'Название неизвестно'}"
                            от {selectedTicket.showtime?.dateTime || 'дата/время неизвестно'}?
                        </Typography>
                    )}
                </DialogContent>

                <DialogActions>
                    <Button onClick={handleCloseDialog} color="secondary" disabled={loading}>
                        Отмена
                    </Button>

                    {dialogType === 'edit' && (
                        <Button onClick={handleConfirmEdit} color="primary" disabled={loading}>
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Сохранить'}
                        </Button>
                    )}

                    {dialogType === 'delete' && (
                        <Button onClick={handleConfirmDelete} color="error" disabled={loading}>
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Удалить'}
                        </Button>
                    )}
                </DialogActions>
            </Dialog>
        </Container>
    );
}

export default MyTicketsPage;

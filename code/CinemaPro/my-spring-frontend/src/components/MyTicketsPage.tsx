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
    Alert, // Для отображения ошибок
    Button, // Возможно понадобится для диалогов
    Dialog, // Для диалогов редактирования/удаления
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField, // Для полей редактирования
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import axios from 'axios';

// --- ОПРЕДЕЛЕНИЕ ТИПОВ ДАННЫХ (ОБНОВЛЕНО ПОД НОВУЮ СТРУКТУРУ JSON) ---
// Эти интерфейсы теперь соответствуют структуре JSON из Postman.

// Тип для пользователя (приходит в JSON на разных уровнях)
interface User {
    id: number;
    username: string;
    email: string;
    // ПРИМЕЧАНИЕ: Пароль по-прежнему приходит в JSON.
    // ЭТО СЕРЬЕЗНАЯ ПРОБЛЕМА БЕЗОПАСНОСТИ, КОТОРУЮ НУЖНО ИСПРАВИТЬ НА БЭКЕНДЕ!
    // На фронтенде мы его не используем, но тип должен его отражать, если он приходит.
    password?: string;
}

// Тип для отзыва (приходит вложенным в Movie)
interface Review {
    id: number;
    rating: number;
    content: string;
    user: User; // Вложенный пользователь в отзыве
    // Добавьте другие поля отзыва, если они приходят
}


// Тип для фильма (приходит вложенным в Showtime)
interface Movie {
    id: number;
    title: string; // Название фильма
    director: string;
    releaseYear: number;
    genre: string;
    reviews: Review[]; // Список отзывов (приходит в JSON)
    // Добавьте другие поля фильма, если они приходят
}

// Тип для сеанса (приходит вложенным в Ticket)
interface Showtime {
    id: number;
    dateTime: string; // Дата и время сеанса (приходит в JSON)
    type: string; // Тип сеанса (2D/3D)
    movie: Movie; // Вложенная сущность Movie
    // В текущем JSON Theater отсутствует, но если появится, добавьте его сюда:
    // theater?: Theater;
    // Список билетов (tickets) скорее всего игнорируется Jackson'ом, поэтому не добавляем его здесь.
}

// Тип для места (приходит вложенным в Ticket)
interface Seat {
    id: number;
    seatRow: number; // В JSON приходит 'seatRow'
    number: number; // В JSON приходит 'number'
    available: boolean;
    // Список билетов (tickets) скорее всего игнорируется Jackson'ом, поэтому не добавляем его здесь.
}

// Основной тип для билета, отражающий реальную структуру JSON
interface Ticket {
    id: number;
    seatNumber: string; // Приходит как строка "Ряд-Место"
    price: number; // Цена билета
    user: User; // Вложенная сущность User (приходит в JSON)
    seat: Seat; // Вложенная сущность Seat (приходит в JSON)
    showtime: Showtime; // Вложенная сущность Showtime (ПРИХОДИТ в новом JSON)
    // Добавьте другие поля билета, если они возвращаются и нужны
    // например, purchaseTime: string;
}
// --- КОНЕЦ ОБНОВЛЕННЫХ ТИПОВ ---


// Определяем пропсы для компонента
interface MyTicketsPageProps {
    currentUser: User | null; // Текущий пользователь
}

// --- URL API ---
// Базовый URL для получения билетов пользователя
const USER_TICKETS_API_URL = 'http://localhost:8080/api/tickets/user';
// --- КОНЕЦ URL API ---


function MyTicketsPage({ currentUser }: MyTicketsPageProps) {
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Состояние для диалога редактирования/удаления
    const [openDialog, setOpenDialog] = useState(false);
    const [dialogType, setDialogType] = useState<'edit' | 'delete' | null>(null);
    // selectedTicket теперь соответствует ОБНОВЛЕННОМУ типу Ticket
    const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
    // editFormData также соответствует ОБНОВЛЕННОМУ типу Ticket (частично)
    const [editFormData, setEditFormData] = useState<Partial<Ticket>>({});

    // --- ФУНКЦИЯ ЗАГРУЗКИ БИЛЕТОВ ПОЛЬЗОВАТЕЛЯ ---
    const fetchTickets = async (userId: number) => {
        setLoading(true);
        setError(null);
        try {
            const timestamp = Date.now(); // Получаем текущее время в миллисекундах
            const url = `${USER_TICKETS_API_URL}/${userId}?_t=${timestamp}`; // Добавляем параметр _t с меткой времени

            console.log(`[fetchTickets] Отправка GET запроса на: ${url}`); // Добавлено логирование URL

            // Выполняем GET запрос к эндпоинту билетов пользователя
            const response = await axios.get<Ticket[]>(url);

            // Проверяем статус ответа
            if (response.status >= 200 && response.status < 300) { // Успешные статусы (2xx)
                if (response.data) {
                    console.log("[fetchTickets] Получены данные:", response.data); // Логирование полученных данных
                    setTickets(response.data);
                } else {
                    console.log("[fetchTickets] Получен пустой ответ или 204 No Content.");
                    // Обработка 204 No Content или пустого массива
                    setTickets([]);
                }
            } else {
                // Обработка других успешных статусов, если необходимо
                console.error(`[fetchTickets] Ошибка при загрузке билетов: Неожиданный статус ${response.status}`, response);
                setError(`Ошибка при загрузке билетов: Статус ${response.status}`);
            }
        } catch (err: any) {
            console.error("Ошибка при загрузке билетов:", err.response?.data || err.message || err);
            // Пытаемся получить сообщение об ошибке из ответа бэкенда, если доступно
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка при загрузке билетов";
            setError(`Не удалось загрузить билеты: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };
    // --- КОНЕЦ ФУНКЦИИ ---

    // --- ЭФФЕКТ ДЛЯ ЗАГРУЗКИ ДАННЫХ ПРИ МОНТИРОВАНИИ И ИЗМЕНЕНИИ ПОЛЬЗОВАТЕЛЯ ---
    useEffect(() => {
        // Загружаем билеты только если пользователь вошел и имеет ID
        if (currentUser?.id) {
            console.log("[useEffect currentUser] currentUser changed or component mounted. Fetching tickets...");
            fetchTickets(currentUser.id);
        } else {
            console.log("[useEffect currentUser] currentUser is null or has no ID. Clearing tickets.");
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
        setEditFormData({}); // Очищаем данные формы при закрытии
        setError(null); // Очищаем ошибки диалога
    };

    // Обработчик изменения полей формы редактирования
    const handleEditFormChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = event.target;
        setEditFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };


    // --- ФУНКЦИИ ДЛЯ ВЫПОЛНЕНИЯ ДЕЙСТВИЙ (РЕДАКТИРОВАНИЕ/УДАЛЕНИЕ) ---

    const handleConfirmEdit = async () => {
        // Проверяем, что есть выбранный билет и текущий пользователь
        if (!selectedTicket || !currentUser) {
            setError("Не удалось выполнить действие: нет выбранного билета или пользователя.");
            return; // Прерываем выполнение
        }

        setLoading(true); // Показываем загрузку во время отправки
        setError(null); // Очищаем предыдущие ошибки

        try {
            // TODO: Убедитесь, что URL и метод соответствуют вашему бэкенду для обновления билета.
            // Обычно это PUT запрос к /api/tickets/{ticketId}.
            // Убедитесь, что бэкенд ожидает формат данных, который вы отправляете (editFormData).
            const updateUrl = `${USER_TICKETS_API_URL.replace('/user', '')}/${selectedTicket.id}`; // Пример: /api/tickets/{ticketId}
            // Отправляем только те поля, которые могут быть изменены
            const dataToSend = {
                price: editFormData.price,
                // Если можно менять место или пользователя, добавьте их ID сюда
                // seatId: editFormData.seat?.id,
                // userId: editFormData.user?.id,
                // showtimeId: selectedTicket.showtime?.id // Сеанс, скорее всего, не меняется
            };
            const response = await axios.put(updateUrl, dataToSend);

            if (response.status >= 200 && response.status < 300) { // Успешные статусы (2xx)
                console.log("Билет успешно обновлен:", response.data);
                // Обновляем список билетов после успешного редактирования
                fetchTickets(currentUser.id); // Перезагружаем список
                handleCloseDialog(); // Закрываем диалог
            } else {
                // Обработка других успешных статусов, если необходимо
                setError(`Ошибка при обновлении билета: Статус ${response.status}`);
            }

        } catch (err: any) {
            console.error("Ошибка при обновлении билета:", err);
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка при обновлении билета";
            setError(`Не удалось обновить билет: ${errorMessage}`);
            // Оставляем диалог открытым, чтобы пользователь увидел ошибку
        } finally {
            setLoading(false); // Скрываем загрузку
        }
    };

    const handleConfirmDelete = async () => {
        // Проверяем, что есть выбранный билет и текущий пользователь
        if (!selectedTicket || !currentUser) {
            setError("Не удалось выполнить действие: нет выбранного билета или пользователя.");
            return; // Прерываем выполнение
        }

        setLoading(true); // Показываем загрузку во время отправки
        setError(null); // Очищаем предыдущие ошибки

        try {
            // TODO: Убедитесь, что URL и метод соответствуют вашему бэкенду для удаления билета.
            // Обычно это DELETE запрос к /api/tickets/{ticketId}.
            const deleteUrl = `${USER_TICKETS_API_URL.replace('/user', '')}/${selectedTicket.id}`; // Пример: /api/tickets/{ticketId}
            const response = await axios.delete(deleteUrl);

            if (response.status === 200 || response.status === 204) { // 200 OK или 204 No Content
                console.log("Билет успешно удален:", selectedTicket.id);
                // Обновляем список билетов после успешного удаления
                fetchTickets(currentUser.id); // Перезагружаем список
                handleCloseDialog(); // Закрываем диалог
            } else {
                // Обработка других успешных статусов, если необходимо
                setError(`Ошибка при удалении билета: Статус ${response.status}`);
            }

        } catch (err: any) {
            console.error("Ошибка при удалении билета:", err);
            const errorMessage = err.response?.data?.message || err.message || "Неизвестная ошибка при удалении билета";
            setError(`Не удалось удалить билет: ${errorMessage}`);
            // Оставляем диалог открытым, чтобы пользователь увидел ошибку
        } finally {
            setLoading(false); // Скрываем загрузку
        }
    };


    // --- РЕНДЕРИНГ КОМПОНЕНТА ---
    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom>
                Мои билеты {/* My Tickets */}
            </Typography>

            {/* Сообщение об ошибке загрузки (вне диалога) */}
            {error && !openDialog && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}

            {/* Индикатор загрузки (вне диалога) */}
            {loading && !openDialog ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <CircularProgress />
                </Box>
            ) : (
                // Отображаем таблицу или сообщение, если билетов нет
                tickets.length > 0 ? (
                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} aria-label="my tickets table">
                            <TableHead>
                                <TableRow>
                                    <TableCell align="center">Фильм</TableCell> {/* Movie */}
                                    <TableCell align="center">Дата</TableCell> {/* Date */}
                                    <TableCell align="center">Время</TableCell> {/* Time */}
                                    <TableCell align="center">Место(а)</TableCell> {/* Seat(s) */}
                                    <TableCell align="center">Цена</TableCell> {/* Price */}
                                    <TableCell align="center">Действия</TableCell> {/* Actions column */}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {tickets.map((ticket) => (
                                    <TableRow
                                        key={ticket.id}
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >
                                        {/* --- ОТОБРАЖЕНИЕ ДАННЫХ ИЗ ВЛОЖЕННЫХ СУЩНОСТЕЙ (ОБНОВЛЕНО) --- */}
                                        <TableCell component="th" scope="row" align="center">
                                            {ticket.showtime?.movie?.title || 'Название фильма неизвестно'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.showtime?.dateTime ?
                                                new Date(ticket.showtime.dateTime).toLocaleDateString() // Форматируем только дату
                                                : 'Дата неизвестна'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {ticket.showtime?.dateTime ?
                                                new Date(ticket.showtime.dateTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) // Форматируем только время (HH:MM)
                                                : 'Время неизвестно'}
                                        </TableCell>
                                        <TableCell align="center">
                                            {/* Доступ к номеру ряда и места через seat - используем РЕАЛЬНЫЕ имена полей из JSON */}
                                            {ticket.seat ? `Ряд ${ticket.seat.seatRow}, Место ${ticket.seat.number}` : 'Место неизвестно'}
                                        </TableCell>
                                        {/* --- КОНЕЦ ОТОБРАЖЕНИЯ ДАННЫХ ИЗ ВЛОЖЕННЫХ СУЩНОСТЕЙ --- */}

                                        <TableCell align="center">{ticket.price?.toFixed(2) || '0.00'} ₽</TableCell> {/* Format price, handle potential null/undefined price */}
                                        {/* Ячейка с кнопками действий */}
                                        <TableCell align="center">
                                            <IconButton
                                                aria-label="delete ticket"
                                                onClick={() => handleDeleteTicket(ticket)}
                                                color="error"
                                                size="small"
                                                sx={{ ml: 1 }} // Небольшой отступ слева
                                                disabled={loading} // Отключаем кнопки во время загрузки
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

            {/* --- ДИАЛОГ РЕДАКТИРОВАНИЯ/УДАЛЕНИЯ --- */}
            {/* Диалог отображается, только если openDialog === true */}
            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>
                    {dialogType === 'edit' ? 'Редактировать билет' : 'Удалить билет'} {/* Edit Ticket : Delete Ticket */}
                </DialogTitle>
                {/* Сообщение об ошибке в диалоге (показываем, только если диалог открыт) */}
                {error && openDialog && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}
                <DialogContent>
                    {dialogType === 'edit' && selectedTicket && (
                        <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            {/* Поля для редактирования билета */}
                            {/* Поля, которые скорее всего нельзя редактировать, делаем disabled */}
                            <TextField
                                margin="dense"
                                label="Фильм" // Movie
                                type="text"
                                fullWidth
                                variant="outlined"
                                value={selectedTicket.showtime?.movie?.title || ''} // Теперь данные о фильме доступны
                                disabled
                            />
                            <TextField
                                margin="dense"
                                label="Дата/Время сеанса" // Showtime Date/Time
                                type="text"
                                fullWidth
                                variant="outlined"
                                value={selectedTicket.showtime?.dateTime || ''} // Теперь дата/время доступны
                                disabled
                            />
                            <TextField
                                margin="dense"
                                label="Место(а)" // Seat(s)
                                type="text"
                                fullWidth
                                variant="outlined"
                                // Используем РЕАЛЬНЫЕ имена полей из JSON
                                value={selectedTicket.seat ? `Ряд ${selectedTicket.seat.seatRow}, Место ${selectedTicket.seat.number}` : ''}
                                disabled // Место обычно нельзя изменить
                            />
                            {/* Поле цены - возможно, можно редактировать? Если нет, сделайте disabled */}
                            <TextField
                                margin="dense"
                                name="price" // Имя поля в editFormData
                                label="Цена" // Price
                                type="number"
                                fullWidth
                                variant="outlined"
                                value={editFormData.price ?? ''} // Используем ?? для обработки undefined/null
                                onChange={handleEditFormChange}
                                // disabled={loading} // Можно отключить во время отправки формы
                            />
                            {/* Добавьте другие поля, которые можно редактировать, если они есть */}
                        </Box>
                    )}
                    {dialogType === 'delete' && selectedTicket && (
                        <Typography variant="body1">
                            Вы уверены, что хотите удалить билет на фильм "
                            {selectedTicket.showtime?.movie?.title || 'Название фильма неизвестно'}"
                            от {selectedTicket.showtime?.dateTime || 'дата/время неизвестно'}?
                            {/* Updated text */}
                        </Typography>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog} color="secondary" disabled={loading}>
                        Отмена {/* Cancel */}
                    </Button>
                    {dialogType === 'edit' && (
                        <Button onClick={handleConfirmEdit} color="primary" disabled={loading}>
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Сохранить'} {/* Save */}
                        </Button>
                    )}
                    {dialogType === 'delete' && (
                        <Button onClick={handleConfirmDelete} color="error" disabled={loading}>
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Удалить'} {/* Delete */}
                        </Button>
                    )}
                </DialogActions>
            </Dialog>

        </Container>
    );
}

export default MyTicketsPage;

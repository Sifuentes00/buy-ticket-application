import React, { useState, useEffect } from 'react'; // useEffect импортирован, т.к. используется для localStorage
// Assuming MoviesTable is your main movie list component
import MoviesTable from './components/MoviesTable';
// Assuming UserProfile component handles login/register buttons and user display
import UserProfile from './components/UserProfile'; // <-- Убедитесь, что импортирован
// Import the Movie Details Page component
import MovieDetailsPage from './components/MovieDetailsPage';
// Import the new pages
import MyTicketsPage from './components/MyTicketsPage'; // <-- Import MyTicketsPage

import { // Import necessary Material UI components
    Container,
    Typography,
    Box,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    CircularProgress,
    IconButton,
    AppBar, // Import AppBar for the header
    Toolbar, // Import Toolbar for the header
    useTheme // Импортируем useTheme для доступа к цветам темы
} from '@mui/material';
import HomeIcon from '@mui/icons-material/Home'; // Import Home icon

// Import routing components and hooks
import { Routes, Route, useNavigate /* Removed Link import */ } from 'react-router-dom'; // Import useNavigate

import axios from 'axios'; // <-- Imported axios

// УДАЛЕНО: Импорт useAuth, поскольку файла AuthContext нет
// import { useAuth } from './context/AuthContext';


// Define User type
interface User {
    id: number;
    username: string;
    email: string;
    password?: string; // Optional on frontend
    // tickets?: Array<{ id: number /* other ticket fields */ }>; // Not needed in App state usually
    // reviews?: Array<{ id: number; rating: number /* other review fields */ }>; // Not needed in App state usually
}

// Define type for login/register form data
interface UserFormData {
    username: string;
    email: string;
    password: string;
}

// URL for your backend users endpoint (for registration POST /api/users)
const USERS_API_URL = 'http://localhost:8080/api/users';

// TODO: Define the URL FOR YOUR LOGIN ENDPOINT on the backend (YOU need to implement it)
const LOGIN_API_URL = 'http://localhost:8080/api/auth/login';


function App() {
    const navigate = useNavigate();
    // ИСПРАВЛЕНО: Управление пользователем через локальное состояние (как в вашем коде)
    const [currentUser, setCurrentUser] = useState<User | null>(null);
    const theme = useTheme(); // Получаем объект темы


    // --- Dialog State (for login/register) ---
    const [dialogType, setDialogType] = useState<'none' | 'login' | 'register'>('none');
    const [formData, setFormData] = useState<UserFormData>({ username: '', email: '', password: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    // Состояние для общей ошибки формы (например, от бэкенда или общей ошибки валидации)
    const [formError, setFormError] = useState<string | null>(null);

    // === ДОБАВЛЕНО: Состояния для ошибок валидации конкретных полей ===
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    // ================================================================


    // --- Effect to check for user on page load ---
    // ИСПРАВЛЕНО: Вернули логику загрузки пользователя из localStorage сюда
    useEffect(() => {
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            try {
                // Parse user data from local storage
                const parsedUser: User = JSON.parse(savedUser);
                // Basic check to ensure it's a valid user object
                if (parsedUser && parsedUser.id && parsedUser.username) {
                    setCurrentUser(parsedUser);
                } else {
                    // Clear invalid data from local storage
                    localStorage.removeItem('currentUser');
                    console.warn("Invalid user data found in local storage.");
                }
            } catch (e) {
                console.error("Error parsing user data from local storage:", e);
                localStorage.removeItem('currentUser');
            }
        }
    }, []); // Зависит только от монтирования


    // --- Dialog Management Logic (login/register) ---
    const handleOpenDialog = (type: 'login' | 'register') => {
        setDialogType(type);
        setFormData({ username: '', email: '', password: '' });
        setFormError(null); // Очищаем общую ошибку
        // === ДОБАВЛЕНО: Очищаем ошибки полей при открытии ===
        setUsernameError(null);
        setEmailError(null);
        setPasswordError(null);
        // ===============================================
    };

    const handleCloseDialog = () => {
        setDialogType('none');
        setFormData({ username: '', email: '', password: '' });
        setFormError(null); // Очищаем общую ошибку
        // === ДОБАВЛЕНО: Очищаем ошибки полей при закрытии ===
        setUsernameError(null);
        setEmailError(null);
        setPasswordError(null);
        // ===============================================
    };

    // --- Form Input Change Logic ---
    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
        // === ДОБАВЛЕНО: Очищаем ошибку для конкретного поля при вводе ===
        // Это дает немедленную обратную связь при исправлении ошибки
        if (name === 'username') setUsernameError(null);
        if (name === 'email') setEmailError(null);
        if (name === 'password') setPasswordError(null);
        setFormError(null); // Очищаем общую ошибку при любом вводе (кроме отправки)
        // ===========================================================
    };

    // --- Form Submission Logic (LOGIN OR REGISTER) ---
    const handleSubmit = async () => {
        // === ДОБАВЛЕНО/ИЗМЕНЕНО: Фронтенд-валидация перед отправкой ===
        let hasError = false; // Флаг наличия ошибок

        // Очищаем предыдущие ошибки полей и общую ошибку
        setUsernameError(null);
        setEmailError(null);
        setPasswordError(null);
        setFormError(null);

        // Валидация поля "Ник (Username)"
        if (!formData.username.trim()) {
            setUsernameError('Ник не может быть пустым');
            hasError = true;
        } else if (formData.username.trim().length < 3) { // Минимальная длина 3 символа для ника осталась
            setUsernameError('Ник должен быть не короче 3 символов');
            hasError = true;
        }

        // Валидация поля "Email" (только для регистрации)
        if (dialogType === 'register') {
            if (!formData.email.trim()) {
                setEmailError('Email не может быть пустым');
                hasError = true;
            } else if (!/\S+@\S+\.\S+/.test(formData.email.trim())) { // Простая проверка формата email
                setEmailError('Введите корректный email');
                hasError = true;
            }
        }

        // Валидация поля "Пароль"
        if (!formData.password.trim()) {
            setPasswordError('Пароль не может быть пустым');
            hasError = true;
        }
        // === ИСПРАВЛЕНО: Удалена проверка минимальной длины пароля ===
        // else if (formData.password.trim().length < 6) {
        //      setPasswordError('Пароль должен быть не короче 6 символов');
        //      hasError = true;
        //  }
        // =============================================================


        // Если есть хотя бы одна ошибка валидации, прерываем отправку
        if (hasError) {
            console.log("Фронтенд-валидация провалена.");
            // setFormError("Пожалуйста, исправьте ошибки в форме."); // Опционально: установить общую ошибку
            return;
        }
        // === Конец фронтенд-валидации ===


        setIsSubmitting(true);
        // setFormError(null); // Уже очистили выше

        try {
            if (dialogType === 'register') {
                // --- REGISTRATION LOGIC ---
                const userDataToSend = {
                    username: formData.username.trim(),
                    email: formData.email.trim(),
                    password: formData.password.trim(),
                };
                // POST request to user creation endpoint
                const response = await axios.post<User>(USERS_API_URL, userDataToSend);

                if (response.status === 201) { // Success on registration (status 201 Created)
                    const registeredUser = response.data;
                    // Автоматический вход после регистрации
                    setCurrentUser(registeredUser);
                    localStorage.setItem('currentUser', JSON.stringify(registeredUser));
                    console.log("User successfully registered:", registeredUser);
                    handleCloseDialog(); // Close dialog
                }
                // Если бэкенд вернет другой 2xx статус, это неожиданно, но будет перехвачено catch

            } else if (dialogType === 'login') {
                // --- LOGIN LOGIC ---
                const loginDataToSend = {
                    username: formData.username.trim(),
                    password: formData.password.trim(),
                };

                // POST request to login endpoint (ВАМ НУЖНО РЕАЛИЗОВАТЬ ЭТОТ ЭНДПОИНТ НА БЭКЕНДЕ!)
                // Ожидается статус 200 OK при успешном входе и данные пользователя в теле ответа
                const response = await axios.post<User>(LOGIN_API_URL, loginDataToSend);

                if (response.status === 200) { // Success on login (status 200 OK)
                    const loggedInUser = response.data;
                    setCurrentUser(loggedInUser);
                    localStorage.setItem('currentUser', JSON.stringify(loggedInUser));
                    console.log("User successfully logged in:", loggedInUser);
                    handleCloseDialog(); // Close dialog
                }
                // Если бэкенд вернет другой 2xx статус, это неожиданно, но будет перехвачено catch
            }

        } catch (error: any) { // <-- Блок обработки ошибок (для статусов 4xx, 5xx и других ошибок)
            console.error(`Error during ${dialogType === 'register' ? 'registration' : 'login'}:`, error.response?.data || error.message || error); // Логируем полную ошибку

            let userFacingErrorMessage = "Произошла ошибка при выполнении запроса."; // Сообщение по умолчанию

            // === ДОБАВЛЕНО: Обработка конкретных ошибок входа ===
            if (dialogType === 'login') {
                const status = error.response?.status;
                const backendMessage = error.response?.data?.message || error.response?.data; // Пытаемся получить сообщение из data

                if (status === 401 || status === 403 || (status === 400 && typeof backendMessage === 'string' && (backendMessage.includes('Invalid') || backendMessage.includes('invalid')))) {
                    // Частые статусы для ошибок авторизации/неверных данных
                    userFacingErrorMessage = "Неверный ник или пароль."; // Более конкретное сообщение
                } else if (status === 404 || (typeof backendMessage === 'string' && (backendMessage.includes('not found') || backendMessage.includes('NotFound')))) {
                    // Если бэкенд явно указывает "не найден"
                    userFacingErrorMessage = "Пользователь не найден.";
                }
                // TODO: Добавьте другие проверки backendMessage, если ваш бэкенд возвращает специфичные строки/коды


            } else { // Обработка ошибок регистрации или других общих ошибок
                const backendMessage = error.response?.data?.message || error.response?.data || error.message;
                userFacingErrorMessage = backendMessage || `Запрос не выполнен (${error.response?.status || error.message})`;
            }
            // === Конец обработки конкретных ошибок ===

            setFormError(userFacingErrorMessage); // === Устанавливаем общую ошибку формы ===


            // TODO: Опционально сбрасывать только поле пароля при ошибке входа
        } finally {
            setIsSubmitting(false); // Сбрасываем состояние отправки формы
        }
    };

    // --- LOGOUT LOGIC ---
    const handleLogout = () => {
        setCurrentUser(null);
        localStorage.removeItem('currentUser');
        console.log("User logged out.");
        navigate('/'); // Перенаправление на домашнюю страницу после выхода
    };

    // --- HOME BUTTON LOGIC ---
    const handleHomeClick = () => {
        console.log("Нажата кнопка Home. Перенаправление на домашнюю страницу ('/').");
        navigate('/');
    };


    // Determine dialog title, button text, and form field visibility based on dialog type
    const dialogTitle = dialogType === 'login' ? 'Вход' : 'Регистрация';
    const submitButtonText = dialogType === 'login' ? 'Войти' : 'Зарегистрироваться';
    const isEmailFieldVisible = dialogType === 'register'; // Email field visible only on registration


    const hoverScaleSx = {
        transition: 'transform 0.2s ease-in-out',
        '&:hover': {
            transform: 'scale(1.05)',
        }
    };


    // ИСПРАВЛЕНО: Проверка авторизации с помощью currentUser
    //const isAuthenticated = currentUser !== null; // Вычисляем флаг авторизации локально


    return (
        // Router is typically wrapped around the entire app in index.tsx or main.tsx
        // If you have <BrowserRouter> in index.tsx, remove it here.
        // If not, keep it here.
        // <Router> // Assuming Router is already in index.tsx/main.tsx

        <Box sx={{ flexGrow: 1 }}>
            {/* --- APP BAR (HEADER) --- */}
            <AppBar position="static">
                <Toolbar>
                    {/* Left part: Home Button */}
                    <IconButton
                        aria-label="home"
                        size="large" // Размер иконки
                        color="inherit" // Цвет иконки наследуется от AppBar
                        onClick={handleHomeClick}
                        sx={{
                            ...hoverScaleSx,
                            mr: 1, // Уменьшен отступ справа для лучшего выравнивания с текстом
                        }}
                    >
                        <HomeIcon fontSize="large" /> {/* Размер иконки */}
                    </IconButton>

                    <Typography
                        variant="h6" // Стандартный стиль заголовка AppBar
                        component="div"
                        sx={{
                            color: theme.palette.primary.contrastText,

                            mr: 2, // Отступ справа от текста до следующего элемента (можно подстроить)
                            fontSize: '2rem', // Увеличен размер шрифта
                            fontWeight: 'bold', // Опционально: сделать жирным
                            // letterSpacing: '0.05em', // Опционально: добавить межбуквенный интервал
                        }}
                    >
                        CinemaPro
                    </Typography>
                    {/* ======================================================== */}


                    {/* Используем Box с flexGrow: 1 для заполнения пространства и выталкивания UserProfile вправо */}
                    <Box sx={{ flexGrow: 1 }} />


                    {/* Right part: User Profile / Login/Register Buttons - Now handled by UserProfile component */}
                    {/* Передаем все необходимые пропсы в UserProfile */}
                    <UserProfile
                        currentUser={currentUser} // Передаем локальное состояние пользователя
                        onLoginClick={() => handleOpenDialog('login')} // Передаем функцию открытия диалога входа
                        onRegisterClick={() => handleOpenDialog('register')} // Передаем функцию открытия диалога регистрации
                        onLogout={handleLogout} // Передаем функцию выхода
                    />

                </Toolbar>
            </AppBar>


            <Container maxWidth="lg" sx={{ my: 2, px: 2 }}>
                {/* --- Here we define the routes --- */}
                <Routes>
                    {/* Route for the root path (main movie list page) */}
                    <Route path="/" element={<MoviesTable />} />

                    {/* Route for the movie details page */}
                    {/* currentUser теперь доступен внутри MovieDetailsPage через useAuth() */}
                    <Route path="/movies/:id" element={<MovieDetailsPage currentUser={currentUser} />} />

                    {/* <-- ДОБАВЛЕНЫ МАРШРУТЫ ДЛЯ СТРАНИЦ ПОЛЬЗОВАТЕЛЯ --> */}
                    {/* Защищаем эти маршруты: если currentUser null, показываем кнопки входа/регистрации */}
                    {/* ИСПРАВЛЕНО: Защита маршрутов с помощью локального состояния currentUser */}
                    <Route
                        path="/my-tickets"
                        element={currentUser ? <MyTicketsPage currentUser={currentUser} /> : <UserProfile onLoginClick={() => handleOpenDialog('login')} onRegisterClick={() => handleOpenDialog('register')} onLogout={handleLogout} currentUser={null} />} // UserProfile покажет кнопки
                    />
                    {/* <-- КОНЕЦ ДОБАВЛЕННЫХ МАРШРУТОВ --> */}

                    {/* TODO: Добавить маршрут для страницы 404 Not Found */}
                </Routes>
            </Container>


            {/* --- DIALOG WINDOW (LOGIN/REGISTER) --- */}
            <Dialog open={dialogType !== 'none'} onClose={handleCloseDialog}>
                <DialogTitle>{dialogTitle}</DialogTitle>
                <DialogContent>
                    {/* Отображение общей ошибки формы (например, от бэкенда) */}
                    {formError && (
                        <Typography color="error" variant="body2" sx={{ mb: 2 }}>
                            {formError}
                        </Typography>
                    )}

                    <Box
                        component="form"
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                        noValidate
                        autoComplete="off"
                    >
                        {/* Username field - always visible */}
                        <TextField
                            autoFocus
                            margin="dense"
                            name="username"
                            label="Ник (Username)"
                            type="text"
                            fullWidth
                            variant="outlined"
                            value={formData.username}
                            onChange={handleInputChange}
                            // === ДОБАВЛЕНО: Пропы error и helperText для отображения ошибки поля ===
                            error={!!usernameError} // true, если есть ошибка для username
                            helperText={usernameError || ' '} // Текст ошибки (или пробел, чтобы не "прыгала" форма)
                            // =====================================================================
                        />
                        {/* Email field - visible only on registration */}
                        {isEmailFieldVisible && (
                            <TextField
                                margin="dense"
                                name="email"
                                label="Email"
                                type="email"
                                fullWidth
                                variant="outlined"
                                value={formData.email}
                                onChange={handleInputChange}
                                // === ДОБАВЛЕНО: Пропы error и helperText для отображения ошибки поля ===
                                error={!!emailError} // true, если есть ошибка для email
                                helperText={emailError || ' '} // Текст ошибки
                                // =====================================================================
                            />
                        )}

                        {/* Password field - always visible */}
                        <TextField
                            margin="dense"
                            name="password"
                            label="Пароль"
                            type="password"
                            fullWidth
                            variant="outlined"
                            value={formData.password}
                            onChange={handleInputChange}
                            // === ДОБАВЛЕНО: Пропы error и helperText для отображения ошибки поля ===
                            error={!!passwordError} // true, если есть ошибка для password
                            helperText={passwordError || ' '} // Текст ошибки
                            // =====================================================================
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog} color="secondary" disabled={isSubmitting}>
                        Отмена
                    </Button>
                    {/* Кнопка отправки формы */}
                    <Button
                        onClick={handleSubmit}
                        color="primary"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? <CircularProgress size={24} color="inherit" /> : submitButtonText}
                    </Button>
                </DialogActions>
            </Dialog>

        </Box>
        // </Router> // Assuming Router is already in index.tsx/main.tsx
    );
}

export default App;

import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import Keycloak from 'keycloak-js';

// Components
import MoviesTable from './components/MoviesTable';
import UserProfile from './components/UserProfile';
import MovieDetailsPage from './components/MovieDetailsPage';
import MyTicketsPage from './components/MyTicketsPage';

// Material UI
import {
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
    AppBar,
    Toolbar,
    useTheme
} from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

import type { AuthUser, UserFormData } from './types';

interface AppProps {
    keycloak: Keycloak;
}

// 🔽 В САМОМ ВЕРХУ ФАЙЛА (после импортов)

interface KeycloakTokenExtended {
    sub?: string;
    email?: string;
    preferred_username?: string;
    realm_access?: {
        roles?: string[];
    };
}

// --- App Component ---
function App({ keycloak }: AppProps) {

    const navigate = useNavigate();
    const theme = useTheme();

    // --- State ---
    const [currentUser, setCurrentUser] = useState<AuthUser>(null);
    const [dialogType, setDialogType] = useState<'none' | 'login' | 'register'>('none');
    const [formData, setFormData] = useState<UserFormData>({ username: '', email: '', password: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formError, setFormError] = useState<string | null>(null);
    const [usernameError, setUsernameError] = useState<string | null>(null);
    const [emailError, setEmailError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);

    useEffect(() => {
        if (keycloak.authenticated && keycloak.tokenParsed) {
            const token = keycloak.tokenParsed as KeycloakTokenExtended;

            if (!token.sub || !token.email) {
                setCurrentUser(null);
                return;
            }

            setCurrentUser({
                id: token.sub,
                username: token.preferred_username ?? token.email,
                email: token.email,
                roles: token.realm_access?.roles ?? []
            });
        } else {
            setCurrentUser(null);
        }
    }, [keycloak.authenticated, keycloak.tokenParsed]);


    const handleCloseDialog = () => {
        setDialogType('none');
        setFormData({ username: '', email: '', password: '' });
        setFormError(null);
        setUsernameError(null);
        setEmailError(null);
        setPasswordError(null);
    };

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = event.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (name === 'username') setUsernameError(null);
        if (name === 'email') setEmailError(null);
        if (name === 'password') setPasswordError(null);
        setFormError(null);
    };

    const handleSubmit = async () => {
        let hasError = false;
        setUsernameError(null);
        setEmailError(null);
        setPasswordError(null);
        setFormError(null);

        if (!formData.username.trim()) {
            setUsernameError('Ник не может быть пустым');
            hasError = true;
        } else if (formData.username.trim().length < 3) {
            setUsernameError('Ник должен быть не короче 3 символов');
            hasError = true;
        }

        if (dialogType === 'register') {
            if (!formData.email.trim()) {
                setEmailError('Email не может быть пустым');
                hasError = true;
            } else if (!/\S+@\S+\.\S+/.test(formData.email.trim())) {
                setEmailError('Введите корректный email');
                hasError = true;
            }
        }

        if (!formData.password.trim()) {
            setPasswordError('Пароль не может быть пустым');
            hasError = true;
        }

        if (hasError) return;

        setIsSubmitting(true);

        try {
            if (dialogType === 'login') {
                await keycloak.login();
            } else {
                // Если регистрация в Keycloak отключена — откроется логин
                await keycloak.register().catch(() => keycloak.login());
            }

            handleCloseDialog();
        } catch {
            setFormError('Не удалось выполнить вход через Keycloak');
        } finally {
            setIsSubmitting(false);
        }
    };

    // --- Logout (через Keycloak) ---
    const handleLogout = async () => {
        try {
            await keycloak.logout();
        } finally {
            setCurrentUser(null);
            navigate('/');
        }
    };

    const handleHomeClick = () => {
        navigate('/');
    };

    const dialogTitle = dialogType === 'login' ? 'Вход' : 'Регистрация';
    const submitButtonText = dialogType === 'login' ? 'Войти' : 'Зарегистрироваться';
    const isEmailFieldVisible = dialogType === 'register';

    const hoverScaleSx = {
        transition: 'transform 0.2s ease-in-out',
        '&:hover': { transform: 'scale(1.05)' }
    };

    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static">
                <Toolbar>
                    <IconButton
                        aria-label="home"
                        size="large"
                        color="inherit"
                        onClick={handleHomeClick}
                        sx={{ ...hoverScaleSx, mr: 1 }}
                    >
                        <HomeIcon fontSize="large" />
                    </IconButton>

                    <Typography
                        variant="h6"
                        component="div"
                        sx={{ color: theme.palette.primary.contrastText, mr: 2, fontSize: '2rem', fontWeight: 'bold' }}
                    >
                        CinemaPro
                    </Typography>

                    <Box sx={{ flexGrow: 1 }} />

                    <UserProfile
                        currentUser={currentUser}
                        onLoginClick={() => keycloak.login()}
                        onRegisterClick={() => keycloak.register()}
                        onLogout={handleLogout}
                    />
                </Toolbar>
            </AppBar>

            <Container maxWidth="lg" sx={{ my: 2, px: 2 }}>
                <Routes>
                    <Route path="/" element={<MoviesTable />} />

                    <Route
                        path="/movies/:id"
                        element={<MovieDetailsPage currentUser={currentUser} />}
                    />

                    <Route
                        path="/my-tickets"
                        element={
                            currentUser
                                ? <MyTicketsPage currentUser={currentUser} />
                                : <Typography sx={{ mt: 4 }}>
                                    Войдите в систему, чтобы увидеть свои билеты.
                                </Typography>
                        }
                    />
                </Routes>
            </Container>

            <Dialog open={dialogType !== 'none'} onClose={handleCloseDialog}>
                <DialogTitle>{dialogTitle}</DialogTitle>
                <DialogContent>
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
                            error={!!usernameError}
                            helperText={usernameError || ' '}
                        />

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
                                error={!!emailError}
                                helperText={emailError || ' '}
                            />
                        )}

                        <TextField
                            margin="dense"
                            name="password"
                            label="Пароль"
                            type="password"
                            fullWidth
                            variant="outlined"
                            value={formData.password}
                            onChange={handleInputChange}
                            error={!!passwordError}
                            helperText={passwordError || ' '}
                        />
                    </Box>
                </DialogContent>

                <DialogActions>
                    <Button onClick={handleCloseDialog} color="secondary" disabled={isSubmitting}>
                        Отмена
                    </Button>

                    <Button onClick={handleSubmit} color="primary" disabled={isSubmitting}>
                        {isSubmitting ? <CircularProgress size={24} color="inherit" /> : submitButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default App;

import React, { useState, useRef } from 'react';
import {
    IconButton,
    Typography,
    Box,
    Paper,
    ClickAwayListener,
    Grow,
    Popper,
    MenuList,
    MenuItem,
    ListItemIcon,
    ListItemText,
    Button, // <-- Импортируем Button
} from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';

import LoginIcon from '@mui/icons-material/Login';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { useNavigate } from 'react-router-dom';

interface User {
    id: number;
    username: string;
    email: string;
    password?: string;
    tickets?: Array<{ id: number  }>;
}

interface UserProfileProps {
    currentUser: User | null;
    onLoginClick: () => void;
    onRegisterClick: () => void;
    onLogout: () => void;
}


function UserProfile({ currentUser, onLoginClick, onRegisterClick, onLogout }: UserProfileProps) {
    const [openProfileMenu, setOpenProfileMenu] = useState(false);
    const anchorRef = useRef<HTMLButtonElement>(null);

    const navigate = useNavigate();


    // --- ЛОГИКА УПРАВЛЕНИЯ ВЫПАДАЮЩИМ МЕНЮ ---
    const handleToggleProfileMenu = () => {
        setOpenProfileMenu((prevOpen) => !prevOpen);
    };

    const handleCloseProfileMenu = (event: Event | React.SyntheticEvent | null) => {
        if (event && anchorRef.current && anchorRef.current.contains(event.target as HTMLElement)) {
            return;
        }
        setOpenProfileMenu(false);
    };


    // --- ЛОГИКА КНОПОК МЕНЮ ПРОФИЛЯ (теперь используют navigate) ---
    const handleMyTickets = () => {
        console.log("Нажата кнопка 'Мои билеты'. Перенаправляем на /my-tickets");
        navigate('/my-tickets');
        handleCloseProfileMenu(null);
    };

    const handleMenuLogout = () => {
        onLogout();
        handleCloseProfileMenu(null);
    }


    // --- РЕНДЕРИНГ КОМПОНЕНТА ---

    // Компонент теперь сам решает, что отображать в зависимости от currentUser
    return (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}> {/* Используем Box для flex-контейнера */}
            {currentUser ? (

                <> {/* Используем фрагмент, так как Box уже является родительским элементом */}
                    {/* "My Tickets" Button - Оставим их здесь для примера, но в App.tsx они были в другом месте AppBar */}
                    {/* Если вы хотите, чтобы они появлялись только когда пользователь вошел, оставьте их тут */}
                    {/* Если они должны быть всегда в AppBar, их нужно оставить в App.tsx */}
                    {/* Для данного исправления, фокусируемся на Login/Register vs Profile Icon */}
                    {/* <Button color="inherit" onClick={() => navigate('/my-tickets')}>
                        Мои билеты
                    </Button> */}
                    {/* УДАЛЕНО: Кнопка "Мои отзывы" */}
                    {/* <Button color="inherit" onClick={() => navigate('/my-reviews')}>
                        Мои отзывы
                    </Button> */}

                    {/* Иконка пользователя - кликабельная для открытия меню */}
                    <IconButton
                        ref={anchorRef}
                        aria-label="account of current user"
                        aria-controls={openProfileMenu ? 'profile-menu-grow' : undefined}
                        aria-haspopup="true"
                        onClick={handleToggleProfileMenu}
                        color="inherit" // Используем inherit, чтобы цвет соответствовал AppBar (синий)
                        size="large"
                    >
                        <AccountCircleIcon fontSize="large" />
                    </IconButton>
                    {/* Имя пользователя рядом с иконкой */}
                    <Typography variant="body1" sx={{ ml: 1, color: 'inherit' }}> {/* Используем inherit */}
                        {currentUser.username}
                    </Typography>

                    {/* Выпадающее меню профиля */}
                    <Popper
                        open={openProfileMenu}
                        anchorEl={anchorRef.current}
                        role={undefined}
                        placement="bottom-end"
                        transition
                        disablePortal
                        sx={{ zIndex: 1300 }}
                    >
                        {({ TransitionProps, placement }) => {
                            const typedProps: { TransitionProps: any; placement: any } = { TransitionProps, placement };
                            return (
                                <ClickAwayListener onClickAway={handleCloseProfileMenu}>
                                    <Grow
                                        {...typedProps.TransitionProps}
                                        style={{ transformOrigin: typedProps.placement === 'bottom-start' ? 'left top' : 'right top' }}
                                    >
                                        <Paper sx={{ minWidth: 200, bgcolor: '#424242', color: '#ffffff' }}>
                                            <MenuList
                                                autoFocusItem={openProfileMenu}
                                                id="profile-menu-list-grow"
                                            >
                                                {/* Отображаем информацию о пользователе в меню */}
                                                <Box sx={{ px: 2, py: 1, borderBottom: '1px solid #616161' }}>
                                                    <Typography variant="subtitle1" fontWeight="bold">{currentUser.username}</Typography>
                                                    <Typography variant="body2" color="textSecondary" sx={{ color: '#bdbdbb' }}>{currentUser.email}</Typography>
                                                </Box>

                                                {/* Пункты меню */}
                                                <MenuItem onClick={handleMyTickets}>
                                                    <ListItemIcon>
                                                        <ConfirmationNumberIcon sx={{ color: '#ffffff' }} /> {/* Иконки в меню оставим белыми для контраста */}
                                                    </ListItemIcon>
                                                    <ListItemText>Мои билеты</ListItemText>
                                                </MenuItem>
                                                {/* УДАЛЕНО: Пункт меню "Мои отзывы" */}
                                                {/* Разделитель */}
                                                <Box sx={{ my: 1, borderBottom: '1px solid #616161' }}></Box>

                                                {/* Кнопка Выйти */}
                                                <MenuItem onClick={handleMenuLogout}>
                                                    <ListItemIcon>
                                                        <ExitToAppIcon sx={{ color: '#ffffff' }} /> {/* Иконки в меню оставим белыми для контраста */}
                                                    </ListItemIcon>
                                                    <ListItemText>Выйти</ListItemText>
                                                </MenuItem>
                                            </MenuList>
                                        </Paper>
                                    </Grow>
                                </ClickAwayListener>
                            );
                        }}
                    </Popper>
                </>
            ) : (
                // --- Если пользователь НЕ вошел ---
                <> {/* Используем фрагмент */}
                    {/* Кнопка "Войти" */}
                    <Button
                        variant="outlined" // <-- Изменено на outlined
                        color="primary" // <-- Изменено на primary для гарантированного синего цвета
                        startIcon={<LoginIcon />}
                        onClick={onLoginClick}
                    >
                        Войти
                    </Button>
                    {/* Кнопка "Зарегистрироваться" */}
                    <Button
                        variant="outlined" // <-- Изменено на outlined
                        color="primary" // <-- Изменено на primary для гарантированного синего цвета
                        startIcon={<PersonAddIcon />}
                        onClick={onRegisterClick}
                    >
                        Зарегистрироваться
                    </Button>
                </>
            )}
        </Box>
    );
}

export default UserProfile;

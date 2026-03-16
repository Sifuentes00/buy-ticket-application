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
    Button,
} from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import LoginIcon from '@mui/icons-material/Login';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import FavoriteIcon from '@mui/icons-material/Favorite';

import { useNavigate } from 'react-router-dom';

import type { AuthUser } from '../types';

interface UserProfileProps {
    currentUser: AuthUser;  // вместо User | null
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

    const handleMyTickets = () => {
        console.log("Нажата кнопка 'Мои билеты'. Перенаправляем на /my-tickets");
        navigate('/my-tickets');
        handleCloseProfileMenu(null);
    };

    const handleMenuLogout = () => {
        handleCloseProfileMenu(null);
        onLogout();
    };

    return (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {currentUser ? (
                <>
                    <IconButton
                        ref={anchorRef}
                        aria-label="account of current user"
                        aria-controls={openProfileMenu ? 'profile-menu-grow' : undefined}
                        aria-haspopup="true"
                        onClick={handleToggleProfileMenu}
                        color="inherit"
                        size="large"
                    >
                        <AccountCircleIcon fontSize="large" />
                    </IconButton>

                    <Typography variant="body1" sx={{ ml: 1, color: 'inherit' }}>
                        {currentUser.username}
                    </Typography>

                    <Popper
                        open={openProfileMenu}
                        anchorEl={anchorRef.current}
                        placement="bottom-end"
                        transition
                        disablePortal
                        sx={{ zIndex: 1300 }}
                    >
                        {({ TransitionProps, placement }) => (
                            <ClickAwayListener onClickAway={handleCloseProfileMenu}>
                                <Grow
                                    {...TransitionProps}
                                    style={{
                                        transformOrigin:
                                            placement === 'bottom-start'
                                                ? 'left top'
                                                : 'right top',
                                    }}
                                >
                                    <Paper sx={{ minWidth: 200, bgcolor: '#424242', color: '#ffffff' }}>
                                        <MenuList autoFocusItem={openProfileMenu}>
                                            <Box sx={{ px: 2, py: 1, borderBottom: '1px solid #616161' }}>
                                                <Typography variant="subtitle1" fontWeight="bold">
                                                    {currentUser.username}
                                                </Typography>
                                                <Typography
                                                    variant="body2"
                                                    sx={{ color: '#bdbdbb' }}
                                                >
                                                    {currentUser.email}
                                                </Typography>
                                            </Box>

                                            <MenuItem onClick={handleMyTickets}>
                                                <ListItemIcon>
                                                    <ConfirmationNumberIcon sx={{ color: '#ffffff' }} />
                                                </ListItemIcon>
                                                <ListItemText>Мои билеты</ListItemText>
                                            </MenuItem>

                                            <MenuItem onClick={() => { navigate('/favorites'); handleCloseProfileMenu(null); }}>
                                                <ListItemIcon>
                                                    <FavoriteIcon sx={{ color: '#ff4081' }} />
                                                </ListItemIcon>
                                                <ListItemText>Избранное</ListItemText>
                                            </MenuItem>

                                            <Box sx={{ my: 1, borderBottom: '1px solid #616161' }}></Box>

                                            <MenuItem onClick={handleMenuLogout}>
                                                <ListItemIcon>
                                                    <ExitToAppIcon sx={{ color: '#ffffff' }} />
                                                </ListItemIcon>
                                                <ListItemText>Выйти</ListItemText>
                                            </MenuItem>
                                        </MenuList>
                                    </Paper>
                                </Grow>
                            </ClickAwayListener>
                        )}
                    </Popper>
                </>
            ) : (
                <>
                    <Button
                        variant="outlined"
                        color="primary"
                        startIcon={<LoginIcon />}
                        onClick={onLoginClick}
                    >
                        Войти
                    </Button>

                    <Button
                        variant="outlined"
                        color="primary"
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

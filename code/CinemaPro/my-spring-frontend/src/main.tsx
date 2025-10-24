import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { BrowserRouter } from 'react-router-dom'; // <-- Импортируем BrowserRouter

// Создаем темную тему Material UI
const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: '#1E90FF', // Светло-синий для основной кнопки (Билеты, Войти)
        },
        secondary: {
            main: '#f48fb1', // Розовый для вторичных элементов (Отмена)
        },
        background: {
            default: '#121212', // Очень темный фон
            paper: '#212121', // Чуть светлее для карточек/диалогов
        },
    },
    typography: {
        // Настройки типографики, если нужны
    },
});


ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <BrowserRouter> {/* <-- Оборачиваем все приложение в BrowserRouter */}
            <ThemeProvider theme={darkTheme}>
                <CssBaseline /> {/* Применяет базовые стили и фон */}
                <App /> {/* Наше основное приложение */}
            </ThemeProvider>
        </BrowserRouter>
    </React.StrictMode>,
);
// main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { BrowserRouter } from 'react-router-dom';
import keycloak from './keycloak';

// --- Темная тема Material UI ---
const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: { main: '#1E90FF' },
        secondary: { main: '#f48fb1' },
        background: { default: '#121212', paper: '#212121' },
    },
});

keycloak
    .init({
        onLoad: "check-sso",
        checkLoginIframe: false,
        pkceMethod: "S256",
    })
    .then(() => {
        ReactDOM.createRoot(document.getElementById("root")!).render(
            <React.StrictMode>
                <BrowserRouter>
                    <ThemeProvider theme={darkTheme}>
                        <CssBaseline />
                        <App keycloak={keycloak} />
                    </ThemeProvider>
                </BrowserRouter>
            </React.StrictMode>
        );
    })
    .catch((error) => {
        console.error("Keycloak init failed", error);
    });

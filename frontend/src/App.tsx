import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Products from "./pages/Products";
import { useAuthStore } from "./store/authStore";
import LogoutButton from "./components/LogoutButton.tsx";
import Manage from "./pages/Manage.tsx";
import {useEffect, useState} from "react";


export default function App() {
    const token = useAuthStore((state) => state.token);
    const restoreSession = useAuthStore((state) => state.restoreSession);
    const [loading, setLoading] = useState(true); // Zustand zum Warten auf `restoreSession`
    useEffect(() => {
        restoreSession(); // Stelle sicher, dass die Admin-Rolle beim Start geladen wird
        setTimeout(() => setLoading(false), 100); //  Warten, bis Auth-Status geladen ist
    }, []);
    if (loading) return <div className="flex items-center justify-center h-screen">Laden...</div>; //  Ladebildschirm
    return (
        <Router>
            <div className="p-4">
                {/* Logout-Button nur anzeigen, wenn eingeloggt */}
                {token && <LogoutButton />}

                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/products" element={<Products />} />
                    <Route path="/manage" element={<Manage />} /> {/* Admin-Seite */}
                </Routes>
            </div>
        </Router>
    );
}

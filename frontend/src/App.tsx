import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Products from "./pages/Products";
import Manage from "./pages/Manage";
import Profile from "./pages/Profile";
import Orders from "./pages/Orders";
import Cart from "./pages/Cart";
import Navbar from "./components/Navbar";
import { useAuthStore } from "./store/authStore";
import { useEffect, useState } from "react";
import Checkout from "./pages/Checkout.tsx";

export default function App() {
    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const restoreSession = useAuthStore((state) => state.restoreSession);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        restoreSession();
        setTimeout(() => setLoading(false), 100); // Sicherstellen, dass Session geladen ist
    }, []);

    if (loading) return <div className="flex items-center justify-center h-screen">Laden...</div>;

    return (
        <Router>
            <Navbar />
            <div className="p-4">
                <Routes>
                    {/* Öffentliche Seiten */}
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={!token ? <Login /> : <Navigate to="/products" />} />
                    <Route path="/register" element={!token ? <Register /> : <Navigate to="/products" />} />
                    <Route path="/products" element={<Products />} />

                    {/* Geschützte Benutzer-Seiten */}
                    <Route path="/profile" element={token ? <Profile /> : <Navigate to="/login" />} />
                    <Route path="/cart" element={token ? <Cart /> : <Navigate to="/login" />} />
                    <Route path="/orders" element={token ? <Orders /> : <Navigate to="/login" />} />
                    <Route path="/checkout" element={<Checkout />} />

                    {/* Admin-geschützte Seiten */}
                    <Route path="/manage" element={isAdmin ? <Manage /> : <Navigate to="/" />} />
                </Routes>
            </div>
        </Router>
    );
}

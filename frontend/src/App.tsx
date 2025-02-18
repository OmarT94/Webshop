import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Orders from "./pages/Orders.tsx";
import Cart from "./pages/Cart";
import { useEffect, useState } from "react";
import Checkout from "./pages/Checkout.tsx";
import ProductSearch from "./pages/ProductSearch.tsx";
import "./App.css";
import { useAuthStore } from "./store/authStore.ts";
import UserProfile from "./pages/UserProfile.tsx";
import AdminOrders from "./pages/AdminOrders.tsx";
import Manage from "./pages/Manage.tsx";

export default function App() {
    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const restoreSession = useAuthStore((state) => state.restoreSession);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        restoreSession();
        setTimeout(() => setLoading(false), 500);
    }, []);

    if (loading) return <div className="flex items-center justify-center h-screen">Laden...</div>;

    return (
        <Router>
            <header className="header-container">
                <h1> Webshop</h1>
                <nav className="nav-container">
                    <a href="/">Home</a>
                    <a href="/search">🔍 Suche</a>
                    {token && !isAdmin ? (
                        <a href="/cart">🛒 Warenkorb</a>
                    ) : null}
                    {token ? (
                        <div className="logout-button-container">
                            <UserProfile />
                        </div>
                    ) : (
                        <div className="login-buttons">
                            <a href="/login">Login</a>
                            <a href="/register">Registrieren</a>
                        </div>
                    )}
                </nav>
            </header>

            <main className="p-4">
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={!token ? <Login /> : <Navigate to="/products" />} />
                    <Route path="/register" element={!token ? <Register /> : <Navigate to="/products" />} />
                    <Route path="/search" element={<ProductSearch />} />
                    <Route path="/orders" element={token ? <Orders /> : <Navigate to="/login" />} />
                    <Route path="/cart" element={token ? <Cart /> : <Navigate to="/login" />} />
                    <Route path="/checkout" element={token ? <Checkout /> : <Navigate to="/login" />} />
                    <Route path="/profile" element={token ? <UserProfile/> : <Navigate to="/login" />} />
                    <Route path="/admin/orders" element={isAdmin ? <AdminOrders /> : <Navigate to="/" />} />
                    <Route path="/manage" element={isAdmin ? <Manage /> : <Navigate to="/" />} />
                </Routes>
            </main>

            <footer>
                <p>© 2024 Webshop. Alle Rechte vorbehalten.</p>
            </footer>
        </Router>
    );
}

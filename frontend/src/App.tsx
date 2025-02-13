import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Products from "./pages/Products";
import Manage from "./pages/Manage";
import Orders from "./pages/Orders.tsx";
import Cart from "./pages/Cart";
import Navbar from "./components/Navbar";
import { useAuthStore } from "./store/authStore";
import { useEffect, useState } from "react";
import Checkout from "./pages/Checkout.tsx";
import ProductSearch from "./pages/ProductSearch.tsx";

import Profil from "./pages/Profil.tsx";
import AdminOrders from "./pages/AdminOrders.tsx";


export default function App() {
    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const restoreSession = useAuthStore((state) => state.restoreSession);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        restoreSession();
        setTimeout(() => setLoading(false), 500); // Längere Verzögerung, um sicherzustellen, dass die Session geladen ist
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
                    <Route path="/search" element={<ProductSearch />} /> {/* Produktsuche */}

                    {/* Geschützte Benutzer-Seiten */}
                    <Route path="/orders" element={token ? <Orders /> : <Navigate to="/login" />} />
                    <Route path="/cart" element={token ? <Cart /> : <Navigate to="/login" />} />
                    <Route path="/checkout" element={token ? <Checkout /> : <Navigate to="/login" />} />
                    <Route path="/profil" element={token ? <Profil /> : <Navigate to="/login" />} /> {/* Neue Profilseite */}

                    {/* Admin-geschützte Seiten */}
                    <Route path="/manage" element={isAdmin ? <Manage /> : <Navigate to="/" />} />
                    <Route path="/admin/orders" element={isAdmin ? <AdminOrders /> : <Navigate to="/" />} />
                </Routes>
            </div>
        </Router>
    );
}
import { Link } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import LogoutButton from "./LogoutButton";
import { useState } from "react";
import { FaUserCircle } from "react-icons/fa";

export default function Navbar() {
    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const [profileMenuOpen, setProfileMenuOpen] = useState(false);

    const toggleProfileMenu = () => setProfileMenuOpen(!profileMenuOpen);

    return (
        <header className="header-container">
            {/* Logo */}
            <div className="logo-container">
                <Link to="/products">🛍️ Webshop</Link>
            </div>

            {/* Suche */}
            <div className="search-container">
                <Link to="/search">🔍 Suche</Link>
            </div>

            {/* Warenkorb + Login/Register oder Profil */}
            <div className="right-menu">
                <Link to="/cart">🛒 Warenkorb</Link>

                {!token ? (
                    <div className="auth-buttons">
                        <Link to="/login">🔐 Login</Link>
                        <Link to="/register">📝 Registrieren</Link>
                    </div>
                ) : (
                    <div className="profile-container">
                        <FaUserCircle className="profile-icon" onClick={toggleProfileMenu} />
                        {profileMenuOpen && (
                            <div className="profile-menu">
                                <ul>
                                    <li><Link to="/profile">👤 Mein Profil</Link></li>
                                    <li><Link to="/orders">📦 Bestellungen</Link></li>
                                    {isAdmin && (
                                        <>
                                            <li><Link to="/manage">⚙️ Admin-Bereich</Link></li>
                                            <li><Link to="/admin/orders">📑 Admin Bestellungen</Link></li>
                                        </>
                                    )}
                                    <li><LogoutButton /></li>
                                </ul>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </header>
    );
}

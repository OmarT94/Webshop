import { useAuthStore } from "../store/authStore";
import { useState } from "react";
import { FaUserCircle } from "react-icons/fa";

export default function Navbar() {
    const token = useAuthStore((state) => state.token);
    const [profileMenuOpen, setProfileMenuOpen] = useState(false);

    const toggleProfileMenu = () => setProfileMenuOpen(!profileMenuOpen);

    return (
        <header className="header-container">
            {/* Logo */}
            <div className="logo-container">
                <a href="/products">🛍️ Webshop</a>
            </div>

            {/* Suche */}
            <div className="search-container">
                <a href="/search">🔍 Suche</a>
            </div>

            {/* Warenkorb + Login/Register oder Profil */}
            <div className="right-menu">
                <a href="/cart">🛒 Warenkorb</a>

                {!token ? (
                    <div className="auth-buttons">
                        <a href="/login">🔐 Login</a>
                        <a href="/register">📝 Registrieren</a>
                    </div>
                ) : (
                    <div className="profile-container">
                        {/* Kein Link mehr → Stattdessen ein Klick-Event für Dropdown */}
                        <FaUserCircle className="profile-icon" onClick={toggleProfileMenu} />

                        {profileMenuOpen && (
                            <div className="profile-menu">
                                <ul>
                                    <li><a href="/profile">👤 Mein Profil</a></li>
                                    <li><a href="/orders">📦 Bestellungen</a></li>


                                </ul>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </header>
    );
}

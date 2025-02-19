import { useState } from "react";
import {Link, useNavigate} from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import { FaUserCircle } from "react-icons/fa";

export default function UserProfile() {
    const name = useAuthStore((state) => state.firstName);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const [menuOpen, setMenuOpen] = useState(false);

    const logout = useAuthStore((state) => state.logout);
    const navigate = useNavigate();
    const handleLogout = () => {
        logout();
        navigate("/"); // Zur Startseite zurück
    };

    return (
        <div className="profile-container">
            <div className="profile-header" onClick={() => setMenuOpen(!menuOpen)}>
                <FaUserCircle className="profile-icon"/>
                <span className="profile-greeting">Hallo {name}</span>
            </div>

            {menuOpen && (
                <div className="profile-menu">
                    <ul>
                        {isAdmin ? (
                            <>
                                <li>
                                    <Link to="/admin/orders" onClick={() => setMenuOpen(false)}>📑 Admin Bestellungen</Link>
                                </li>
                                <li>
                                    <Link to="/manage" onClick={() => setMenuOpen(false)}>🛠️ Produktverwaltung</Link>
                                </li>
                            </>
                        ) : (
                            <>
                                <li>
                                    <Link to="/orders" onClick={() => setMenuOpen(false)}>📦 Bestellungen anzeigen</Link>
                                </li>
                                <li>
                                    <Link to="/profile/address" onClick={() => setMenuOpen(false)}>🏡 Adresse bearbeiten</Link>
                                </li>
                                <li>
                                    <Link to="/profile/password" onClick={() => setMenuOpen(false)}>🔑 Passwort ändern</Link>
                                </li>
                            </>
                        )}
                        <li>
                            <button onClick={handleLogout} className="logout-button">
                                Logout
                            </button>
                        </li>
                    </ul>
                </div>
            )}
        </div>
    );
}

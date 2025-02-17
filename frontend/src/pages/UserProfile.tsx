import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import { FaUserCircle } from "react-icons/fa";

export default function UserProfile() {
    const email = useAuthStore((state) => state.tokenEmail);
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const [menuOpen, setMenuOpen] = useState(false);

    if (isAdmin) {
        return <div className="">⚠️ Admins haben keinen Zugriff auf die Benutzerprofilseite.</div>;
    }

    return (
        <div className="profile-page">
            <div className="profile-header" onClick={() => setMenuOpen(!menuOpen)}>
                <FaUserCircle className="profile-icon" />
                <h2 className="profile-title">👋 Willkommen, {email}!</h2>
            </div>

            {menuOpen && (
                <div className="profile-dropdown">
                    <ul>
                        <li>
                            <Link to="/profile/orders" onClick={() => setMenuOpen(false)}>📦 Bestellungen anzeigen</Link>
                        </li>
                        <li>
                            <Link to="/profile/address" onClick={() => setMenuOpen(false)}>🏡 Adresse bearbeiten</Link>
                        </li>
                        <li>
                            <Link to="/profile/password" onClick={() => setMenuOpen(false)}>🔑 Passwort ändern</Link>
                        </li>
                    </ul>
                </div>
            )}
        </div>
    );
}

import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";

export default function LogoutButton() {
    const logout = useAuthStore((state) => state.logout);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/"); // Zur Startseite zurück
    };

    return (
        <button onClick={handleLogout} className="logout-button">
            Logout
        </button>
    );
}

import { useAuthStore } from "../store/authStore";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";

export default function Manage() {
    const isAdmin = useAuthStore((state) => state.isAdmin);
    const navigate = useNavigate();

    // Falls kein Admin, automatisch umleiten
    useEffect(() => {
        if (!isAdmin) {
            navigate("/"); // Zur Startseite umleiten
        }
    }, [isAdmin, navigate]);

    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <h2 className="text-2xl font-bold">Admin-Management</h2>
            <p>Hier kann der Admin Produkte verwalten.</p>
        </div>
    );
}

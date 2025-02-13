import { Link } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import LogoutButton from "./LogoutButton";

export default function Navbar() {
    const token = useAuthStore((state) => state.token);
    const isAdmin = useAuthStore((state) => state.isAdmin);

    return (
        <nav className="p-4 bg-gray-800 text-white flex justify-between">
            <Link to="/products" className="text-lg font-bold">Webshop</Link>
            <div className="flex gap-4">
                {!token ? (
                    <>
                        <Link to="/login">Login</Link>
                        <Link to="/register">Registrieren</Link>
                    </>
                ) : (
                    <>
                        <Link to="/cart">🛒 Warenkorb</Link>
                        <Link to="/profil">Profil</Link>
                        <Link to="/orders">Bestellungen</Link>
                        <Link to="/search" className="mr-4">🔍 Suche</Link>
                        {isAdmin && (
                            <>
                                <Link to="/manage">Admin-Bereich</Link>
                                <Link to="/admin/orders">Admin Bestellungen</Link>
                            </>
                        )}
                        <LogoutButton />
                    </>
                )}
            </div>
        </nav>
    );
}
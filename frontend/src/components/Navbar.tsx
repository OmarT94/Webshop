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
                        <Link to="/cart">🛒 Warenkorb</Link> {/*  Warenkorb-Link hinzugefügt */}

                        <Link to="/profile">Profil</Link>
                        {isAdmin && (
                            <>
                                <Link to="/manage">Manage</Link>
                                <Link to="/orders">Bestellungen</Link> {/*  Orders-Seite */}
                            </>
                        )}
                        <LogoutButton />
                    </>
                )}
            </div>
        </nav>
    );
}

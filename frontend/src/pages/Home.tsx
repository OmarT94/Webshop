import { Link } from "react-router-dom";

export default function Home() {
    return (
        <div className="flex flex-col items-center justify-center h-screen gap-4">
            <h2 className="text-2xl font-bold">Willkommen!</h2>
            <p>Bitte wähle eine Option:</p>
            <div className="flex gap-4">
                <Link to="/login">
                    <button className="p-2 bg-blue-500 text-white rounded">Login</button>
                </Link>
                <Link to="/register">
                    <button className="p-2 bg-green-500 text-white rounded">Registrieren</button>
                </Link>
            </div>
        </div>
    );
}

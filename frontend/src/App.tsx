import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Products from "./pages/Products";
import { useAuthStore } from "./store/authStore";
import LogoutButton from "./Components/LogoutButton.tsx";


export default function App() {
    const token = useAuthStore((state) => state.token);

    return (
        <Router>
            <div className="p-4">
                {/* Logout-Button nur anzeigen, wenn eingeloggt */}
                {token && <LogoutButton />}

                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/products" element={<Products />} />
                </Routes>
            </div>
        </Router>
    );
}

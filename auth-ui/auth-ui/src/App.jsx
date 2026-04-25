import { useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8083";

function App() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [message, setMessage] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));

  const handleRegister = async () => {
    try {
      const response = await fetch(`${API_URL}/login/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, password }),
      });

      const data = await response.text();
      setMessage(data);
    } catch {
      setMessage("Register işlemi başarısız.");
    }
  };

  const handleLogin = async () => {
    try {
      const response = await fetch(`${API_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        setMessage("Login başarısız. Kullanıcı adı veya şifre hatalı.");
        return;
      }

      const data = await response.json();

      localStorage.setItem("token", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);

      setIsLoggedIn(true);
      setMessage("Login başarılı.");
    } catch {
      setMessage("Login işlemi sırasında hata oluştu.");
    }
  };

  const callHello = async () => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`${API_URL}/hello`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const data = await response.text();

      if (!response.ok) {
        setMessage(`Hello endpoint hata: ${response.status}`);
        return;
      }

      setMessage(data);
    } catch {
      setMessage("Hello endpoint çağrısı başarısız.");
    }
  };

  const callAdmin = async () => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`${API_URL}/admin`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const data = await response.text();

      if (!response.ok) {
        setMessage(`Admin endpoint erişim reddedildi: ${response.status}`);
        return;
      }

      setMessage(data);
    } catch {
      setMessage("Admin endpoint çağrısı başarısız.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    setIsLoggedIn(false);
    setMessage("Çıkış yapıldı.");
  };

return (
  <div className="page">
    <div className="container">
      <header className="header">
        <div className="brand">
          <div className="logo">A</div>
          <div>
            <h1>Auth Console</h1>
            <p>JWT, Refresh Token, Role Authorization ve RabbitMQ test paneli</p>
          </div>
        </div>

        <span className={isLoggedIn ? "status active" : "status passive"}>
          {isLoggedIn ? "Authenticated" : "Guest Session"}
        </span>
      </header>

      <section className="hero">
        <main className="grid">
          <section className="card">
            <h2>Account Access</h2>
            <p className="cardDescription">
              Kullanıcı oluştur, giriş yap ve JWT token akışını test et.
            </p>

            <div className="formGrid">
              <div className="formGroup">
                <label>Username</label>
                <input
                  placeholder="serdar"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>

              <div className="formGroup">
                <label>Email</label>
                <input
                  placeholder="serdar@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div className="formGroup">
                <label>Password</label>
                <input
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            <div className="buttonGroup">
              <button onClick={handleRegister} className="secondary">
                Register
              </button>
              <button onClick={handleLogin} className="primary">
                Login
              </button>
            </div>
          </section>

          <section className="card">
            <h2>API Actions</h2>
            <p className="cardDescription">
              Saklanan access token ile protected endpointleri çağır.
            </p>

            <div className="actions">
              <div className="actionItem">
                <strong>User Endpoint</strong>
                <span>Token sahibi tüm kullanıcılar erişebilir.</span>
                <button onClick={callHello} className="primary">
                  Call /hello
                </button>
              </div>

              <div className="actionItem">
                <strong>Admin Endpoint</strong>
                <span>Sadece ROLE_ADMIN yetkisi olan kullanıcılar erişebilir.</span>
                <button onClick={callAdmin} className="danger">
                  Call /admin
                </button>
              </div>

              <button onClick={handleLogout} className="secondary">
                Logout
              </button>
            </div>
          </section>
        </main>

        <section className="messageBox">
          <strong>Response</strong>
          <p>{message || "Henüz işlem yapılmadı."}</p>
        </section>
      </section>
    </div>
  </div>
);
}

export default App;
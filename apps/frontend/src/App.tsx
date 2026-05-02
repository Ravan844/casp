import { useState } from "react";
import "./App.css";

type ScanResult = {
  url: string;
  status: "SAFE" | "SUSPICIOUS" | "DANGEROUS";
  score: number;
  issues: string[];
};

function App() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState<ScanResult | null>(null);
  const [history, setHistory] = useState<ScanResult[]>([]);

  const handleAnalyze = async () => {
    if (!url.trim()) return;

    try {
      const response = await fetch("/api/analyze", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ url: url.trim() }),
      });

      const data = await response.json();

      setResult(data);
      setHistory((prev) => [data, ...prev].slice(0, 5));
    } catch (error) {
      console.error("Backend connection error:", error);
      alert("Backend işləmir. Əvvəl backend serveri başladın.");
    }
  };

  return (
    <main className="page">
      <section className="hero">
        <div>
          <p className="eyebrow">LinkGuardian</p>
          <h1>Check suspicious links before you click.</h1>
          <p className="subtitle">
            Paste any link and get an instant risk score, threat level, and clear
            reasons why it may be dangerous.
          </p>
        </div>

        <div className="heroBadge">
          <span>🛡️</span>
          <strong>Backend-powered URL Risk Scan</strong>
        </div>
      </section>

      <section className="scanner">
        <div className="inputPanel">
          <p className="eyebrow">Scan URL</p>
          <h2>Analyze a link</h2>

          <div className="inputRow">
            <input
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://example.com or http://secure-paypal-login-verification.com"
            />
            <button onClick={handleAnalyze}>Analyze Link</button>
          </div>

          <div className="examples">
            <button onClick={() => setUrl("http://secure-paypal-login-verification.com")}>
              Try dangerous example
            </button>
            <button onClick={() => setUrl("https://www.microsoft.com")}>
              Try safe example
            </button>
          </div>
        </div>

        <div className={`resultPanel ${result?.status.toLowerCase() || ""}`}>
          {!result ? (
            <>
              <p className="eyebrow">Result</p>
              <h2>No scan yet</h2>
              <p className="muted">Enter a URL to see the backend risk analysis.</p>
            </>
          ) : (
            <>
              <p className="eyebrow">Result</p>
              <div className="resultTop">
                <h2>{result.status}</h2>
                <div className="score">{result.score}%</div>
              </div>

              <div className="meter">
                <div style={{ width: `${result.score}%` }} />
              </div>

              <h3>Detected issues</h3>
              <ul>
                {result.issues.map((issue) => (
                  <li key={issue}>{issue}</li>
                ))}
              </ul>
            </>
          )}
        </div>
      </section>

      <section className="history">
        <div>
          <p className="eyebrow">Recent Scans</p>
          <h2>Scan history</h2>
        </div>

        {history.length === 0 ? (
          <p className="muted">No scanned links yet.</p>
        ) : (
          <div className="historyList">
            {history.map((scan, index) => (
              <div className="historyItem" key={`${scan.url}-${index}`}>
                <span className={scan.status.toLowerCase()}>{scan.status}</span>
                <p>{scan.url}</p>
                <strong>{scan.score}%</strong>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default App;

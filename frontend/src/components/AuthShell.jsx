export default function AuthShell({ eyebrow, title, subtitle, footer, children }) {
  return (
    <main className="auth-shell">
      <section className="auth-card">
        {eyebrow ? <p className="auth-kicker">{eyebrow}</p> : null}
        <h1 className="auth-title">{title}</h1>
        {subtitle ? <p className="auth-copy">{subtitle}</p> : null}
        {children}
        {footer ? <div className="auth-footer">{footer}</div> : null}
      </section>
    </main>
  );
}

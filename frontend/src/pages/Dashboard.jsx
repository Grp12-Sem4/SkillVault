import AppNavigation from "../components/AppNavigation.jsx";
import CreditDisplay from "../components/CreditDisplay.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { Link } from "react-router-dom";

const pageStyle = {
	minHeight: "100vh",
	padding: "2rem",
	backgroundColor: "#f5f5f5",
};

const panelStyle = {
	maxWidth: "960px",
	margin: "0 auto",
	padding: "2rem",
	border: "1px solid #d4d4d8",
	borderRadius: "12px",
	backgroundColor: "#ffffff",
};

const heroStyle = {
	display: "flex",
	justifyContent: "space-between",
	alignItems: "flex-start",
	gap: "1.5rem",
	flexWrap: "wrap",
	marginBottom: "1.75rem",
};

const actionGridStyle = {
	display: "grid",
	gap: "1rem",
	gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
	marginTop: "1.5rem",
};

const actionCardStyle = {
	display: "grid",
	gap: "0.5rem",
	padding: "1rem",
	borderRadius: "14px",
	border: "1px solid #e2e8f0",
	backgroundColor: "#f8fafc",
	color: "#0f172a",
	textDecoration: "none",
};

const actionTitleStyle = {
	margin: 0,
	fontSize: "1.05rem",
	fontWeight: 700,
};

const actionCopyStyle = {
	margin: 0,
	color: "#475569",
	fontSize: "0.95rem",
};

const ACTIONS = [
	{
		to: "/skills",
		title: "Skills",
		description: "Add offered or requested skills and track practice progress.",
	},
	{
		to: "/knowledge",
		title: "Knowledge",
		description: "Create topics and review what needs another pass.",
	},
	{
		to: "/trades",
		title: "Trades",
		description: "Request sessions, accept offers, and complete live trades.",
	},
	{
		to: "/time-capsules",
		title: "Time Capsules",
		description:
			"Take snapshots of your profile and track skill progression over time.",
	},
];

export default function Dashboard() {
	const { user } = useAuth();
	const userIdentifier = user?.email ?? user?.sub ?? "User";

	return (
		<main className="app-page" style={pageStyle}>
			<section className="app-layout">
				<div
					className="app-panel app-hero-panel app-panel-soft"
					style={panelStyle}
				>
					<AppNavigation />

					<div style={heroStyle}>
						<div style={{ maxWidth: "540px" }}>
							<p className="app-eyebrow">Overview</p>
							<h1 style={{ marginTop: 0, marginBottom: "0.75rem" }}>
								Dashboard
							</h1>
							<p className="app-subtle-text" style={{ margin: 0 }}>
								Welcome to Skill Vault, {userIdentifier}.
							</p>
							<p className="app-subtle-text" style={{ marginTop: "0.9rem" }}>
								Skill Vault is a demo app for tracking skills, keeping knowledge
								fresh, and exchanging help through a credit-based trade
								marketplace.
							</p>
						</div>

						<CreditDisplay />
					</div>

					<section>
						<h2 style={{ marginTop: 0, marginBottom: "0.75rem" }}>
							Main Areas
						</h2>
						<p className="app-subtle-text" style={{ margin: 0 }}>
							Use the sections below to move through the demo quickly.
						</p>

						<div style={actionGridStyle}>
							{ACTIONS.map((action) => (
								<Link
									key={action.to}
									to={action.to}
									className="app-link-card"
									style={actionCardStyle}
								>
									<p style={actionTitleStyle}>{action.title}</p>
									<p style={actionCopyStyle}>{action.description}</p>
								</Link>
							))}
						</div>
					</section>
				</div>
			</section>
		</main>
	);
}

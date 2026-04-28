import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "../api/axiosConfig.js";
import AppNavigation from "../components/AppNavigation.jsx";

const TIMECAPSULES_QUERY_KEY = ["time-capsules"];

const pageStyle = {
	minHeight: "100vh",
	padding: "2rem",
	backgroundColor: "#f5f5f5",
};
const panelStyle = {
	backgroundColor: "#ffffff",
	border: "1px solid #d4d4d8",
	borderRadius: "16px",
	padding: "1.5rem",
	marginBottom: "1.5rem",
};
const buttonStyle = {
	padding: "0.8rem 1rem",
	borderRadius: "10px",
	border: "none",
	backgroundColor: "#2563eb",
	color: "#ffffff",
	fontWeight: 600,
	cursor: "pointer",
};
const inputStyle = {
	width: "100%",
	padding: "0.8rem 0.9rem",
	borderRadius: "10px",
	border: "1px solid #cbd5e1",
	boxSizing: "border-box",
	marginTop: "0.35rem",
};

function getErrorMessage(error, fallbackMessage) {
	if (error?.response?.status === 401) {
		return "Your session is missing or expired. Log in again and retry the Time Capsule action.";
	}

	return error?.response?.data?.message ?? fallbackMessage;
}

export default function TimeCapsules() {
	const queryClient = useQueryClient();
	const [label, setLabel] = useState("");
	const [baseId, setBaseId] = useState("");
	const [targetId, setTargetId] = useState("");

	const { data: snapshots = [], isLoading } = useQuery({
		queryKey: TIMECAPSULES_QUERY_KEY,
		queryFn: async () => {
			const response = await api.get("/api/time-capsules");
			return response.data ?? [];
		},
	});

	const createSnapshotMutation = useMutation({
		mutationFn: async (payload) => {
			const response = await api.post("/api/time-capsules", payload);
			return response.data;
		},
		onSuccess: () => {
			setLabel("");
			setBaseId("");
			setTargetId("");
			queryClient.invalidateQueries({ queryKey: TIMECAPSULES_QUERY_KEY });
		},
	});

	const compareMutation = useMutation({
		mutationFn: async (payload) => {
			const response = await api.post("/api/time-capsules/compare", payload);
			return response.data;
		},
	});

	const handleCreate = (e) => {
		e.preventDefault();
		if (!label.trim()) return;
		createSnapshotMutation.mutate({ name: label.trim() });
	};

	const handleCompare = (e) => {
		e.preventDefault();
		if (!baseId || !targetId || baseId === targetId) return;
		compareMutation.mutate({
			oldSnapshotId: Number(baseId),
			newSnapshotId: Number(targetId),
		});
	};

	const canCompare = snapshots.length >= 2 && baseId && targetId && baseId !== targetId;

	return (
		<main className="app-page" style={pageStyle}>
			<section
				className="app-layout"
				style={{ maxWidth: "1120px", margin: "0 auto" }}
			>
				<AppNavigation />

				<header className="app-panel" style={panelStyle}>
					<h1 style={{ marginTop: 0 }}>Time Capsules</h1>
					<p className="app-subtle-text">
						Snapshot your current skills and knowledge, and compare them over
						time.
					</p>
				</header>

				<div
					style={{
						display: "grid",
						gridTemplateColumns: "1fr 1fr",
						gap: "1.5rem",
					}}
				>
					{/* Create Snapshot Form */}
					<section className="app-panel" style={panelStyle}>
						<h2 style={{ marginTop: 0 }}>Create Snapshot</h2>
						<form onSubmit={handleCreate}>
							<div className="field-group" style={{ marginBottom: "1rem" }}>
								<label className="field-label">Snapshot Label</label>
								<input
									type="text"
									value={label}
									onChange={(e) => setLabel(e.target.value)}
									style={inputStyle}
									placeholder="e.g., Pre-Hackathon State"
									required
								/>
							</div>
							<button
								type="submit"
								style={buttonStyle}
								disabled={createSnapshotMutation.isPending}
							>
								{createSnapshotMutation.isPending
									? "Saving..."
									: "Save Snapshot"}
							</button>
							{createSnapshotMutation.isError ? (
								<p className="app-feedback-error">
									{getErrorMessage(
										createSnapshotMutation.error,
										"Unable to create the snapshot.",
									)}
								</p>
							) : null}
						</form>
					</section>

					{/* Compare Snapshots Form */}
					<section className="app-panel" style={panelStyle}>
						<h2 style={{ marginTop: 0 }}>Compare Progress</h2>
						<form onSubmit={handleCompare}>
							<div
								style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}
							>
								<div style={{ flex: 1 }}>
									<label className="field-label">Base Snapshot</label>
									<select
										value={baseId}
										onChange={(e) => setBaseId(e.target.value)}
										style={inputStyle}
										required
									>
										<option value="">Select Base</option>
										{snapshots.map((s) => (
											<option key={s.id} value={s.id}>
												{s.name} ({new Date(s.createdAt).toLocaleDateString()})
											</option>
										))}
									</select>
								</div>
								<div style={{ flex: 1 }}>
									<label className="field-label">Target Snapshot</label>
									<select
										value={targetId}
										onChange={(e) => setTargetId(e.target.value)}
										style={inputStyle}
										required
									>
										<option value="">Select Target</option>
										{snapshots.map((s) => (
											<option key={s.id} value={s.id}>
												{s.name} ({new Date(s.createdAt).toLocaleDateString()})
											</option>
										))}
									</select>
								</div>
							</div>
							<button
								type="submit"
								style={buttonStyle}
								disabled={compareMutation.isPending || !canCompare}
							>
								{compareMutation.isPending ? "Comparing..." : "Run Comparison"}
							</button>
							{snapshots.length < 2 ? (
								<p className="app-subtle-text">
									Create at least two snapshots before comparing progress.
								</p>
							) : null}
							{baseId && targetId && baseId === targetId ? (
								<p className="app-feedback-error">
									Select two different snapshots.
								</p>
							) : null}
							{compareMutation.isError ? (
								<p className="app-feedback-error">
									{getErrorMessage(
										compareMutation.error,
										"Unable to compare snapshots.",
									)}
								</p>
							) : null}
						</form>
					</section>
				</div>

				<section className="app-panel" style={panelStyle}>
					<h2 style={{ marginTop: 0 }}>Saved Snapshots</h2>
					{isLoading ? <p>Loading snapshots...</p> : null}
					{!isLoading && snapshots.length === 0 ? (
						<p className="app-subtle-text">
							No snapshots yet. Create one from the form above.
						</p>
					) : null}
					{snapshots.length > 0 ? (
						<ul>
							{snapshots.map((snapshot) => (
								<li key={snapshot.id}>
									<strong>{snapshot.name}</strong> -{" "}
									{new Date(snapshot.createdAt).toLocaleString()} -{" "}
									{snapshot.skillCount} skills, {snapshot.knowledgeTopicCount}{" "}
									knowledge topics
								</li>
							))}
						</ul>
					) : null}
				</section>

				{/* Comparison Results */}
				{compareMutation.isSuccess && compareMutation.data && (
					<section className="app-panel" style={panelStyle}>
						<h2 style={{ marginTop: 0 }}>Comparison Results</h2>
						<p className="app-subtle-text">
							{compareMutation.data.oldSnapshot?.name} to{" "}
							{compareMutation.data.newSnapshot?.name}
						</p>

						<h3>Skill Changes</h3>
						{compareMutation.data.commonSkills?.length > 0 ? (
							<ul>
								{compareMutation.data.commonSkills.map((c) => (
									<li key={c.sourceSkillId ?? c.skillName}>
										<strong>{c.skillName}</strong>: mastery{" "}
										{c.oldMasteryLevel} to {c.newMasteryLevel}; confidence{" "}
										{c.oldConfidenceIndex?.toFixed(1)} to{" "}
										{c.newConfidenceIndex?.toFixed(1)}.
									</li>
								))}
							</ul>
						) : (
							<p>No skill changes detected.</p>
						)}

						<h3>Knowledge Changes</h3>
						{compareMutation.data.commonKnowledgeTopics?.length > 0 ? (
							<ul>
								{compareMutation.data.commonKnowledgeTopics.map((c) => (
									<li key={c.sourceTopicId ?? c.topicName}>
										<strong>{c.topicName}</strong>: mastery{" "}
										{c.oldMasteryLevel?.toFixed(1)} to{" "}
										{c.newMasteryLevel?.toFixed(1)}; retrievability{" "}
										{c.oldRetrievabilityScore?.toFixed(1)} to{" "}
										{c.newRetrievabilityScore?.toFixed(1)}.
									</li>
								))}
							</ul>
						) : (
							<p>No knowledge changes detected.</p>
						)}
					</section>
				)}
			</section>
		</main>
	);
}

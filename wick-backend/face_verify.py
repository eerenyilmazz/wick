#!/usr/bin/env python3
"""
Wick Face Verification Daemon
Compares verification selfies with profile photos using DeepFace.
Runs continuously, polling for PENDING verifications every 60 seconds.

Requirements:
    pip install deepface psycopg2-binary requests

Usage:
    python face_verify.py
"""

import os
import time
import logging
import requests
import psycopg2

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)

# ── Config ────────────────────────────────────────────────────────────────────
DB_URL = os.getenv("DB_URL", "postgresql://wick:wick@localhost:5432/wick")
PHOTO_DIR = os.getenv("PHOTO_DIR", "./data/photos")
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")
INTERNAL_KEY = os.getenv("INTERNAL_KEY", "wick-internal-secret-change-in-prod")
POLL_INTERVAL = int(os.getenv("POLL_INTERVAL", "60"))
MATCH_THRESHOLD = float(os.getenv("MATCH_THRESHOLD", "0.6"))  # DeepFace distance threshold


def get_connection():
    return psycopg2.connect(DB_URL)


def fetch_pending_users(conn):
    with conn.cursor() as cur:
        cur.execute(
            "SELECT id FROM users WHERE verification_status = 'PENDING' AND deleted_at IS NULL"
        )
        return [str(row[0]) for row in cur.fetchall()]


def get_profile_photo_keys(conn, user_id: str) -> list[str]:
    with conn.cursor() as cur:
        cur.execute(
            "SELECT storage_key FROM user_photos WHERE user_id = %s ORDER BY display_order",
            (user_id,),
        )
        return [row[0] for row in cur.fetchall()]


def faces_match(selfie_path: str, profile_paths: list[str]) -> bool:
    """Returns True if the selfie matches at least one profile photo."""
    from deepface import DeepFace

    for profile_path in profile_paths:
        if not os.path.exists(profile_path):
            log.warning("Profile photo not found: %s", profile_path)
            continue
        try:
            result = DeepFace.verify(
                img1_path=selfie_path,
                img2_path=profile_path,
                model_name="Facenet512",
                detector_backend="retinaface",
                distance_metric="cosine",
                enforce_detection=True,
                silent=True,
            )
            if result["verified"] and result["distance"] <= MATCH_THRESHOLD:
                log.info("Match found (distance=%.3f) against %s", result["distance"], profile_path)
                return True
        except Exception as e:
            log.warning("DeepFace error comparing %s: %s", profile_path, e)

    return False


def call_backend(user_id: str, action: str):
    """Calls /internal/verify/{userId}/approve or /reject"""
    url = f"{BACKEND_URL}/internal/verify/{user_id}/{action}"
    try:
        resp = requests.post(url, headers={"X-Internal-Key": INTERNAL_KEY}, timeout=10)
        if resp.status_code == 200:
            log.info("User %s → %s OK", user_id, action)
        else:
            log.warning("Backend returned %d for user %s (%s)", resp.status_code, user_id, action)
    except Exception as e:
        log.error("Failed to call backend for user %s: %s", user_id, e)


def process_user(conn, user_id: str):
    selfie_path = os.path.join(PHOTO_DIR, "verification", f"{user_id}.jpg")
    if not os.path.exists(selfie_path):
        log.warning("Selfie not found for user %s, skipping", user_id)
        return

    profile_keys = get_profile_photo_keys(conn, user_id)
    if not profile_keys:
        log.info("User %s has no profile photos, rejecting", user_id)
        call_backend(user_id, "reject")
        return

    profile_paths = [os.path.join(PHOTO_DIR, key) for key in profile_keys]
    log.info("Comparing selfie for user %s against %d profile photo(s)", user_id, len(profile_paths))

    matched = faces_match(selfie_path, profile_paths)
    call_backend(user_id, "approve" if matched else "reject")


def run():
    log.info("Face verification daemon starting (poll=%ds, threshold=%.2f)", POLL_INTERVAL, MATCH_THRESHOLD)
    while True:
        try:
            with get_connection() as conn:
                pending = fetch_pending_users(conn)
                if pending:
                    log.info("Found %d pending verification(s)", len(pending))
                    for user_id in pending:
                        process_user(conn, user_id)
                else:
                    log.debug("No pending verifications")
        except Exception as e:
            log.error("Error in verification loop: %s", e)

        time.sleep(POLL_INTERVAL)


if __name__ == "__main__":
    run()

from auth import hash_password, hash_refresh_token, verify_password


def test_password_hash_is_not_the_original_password():
    encoded = hash_password("a-long-test-password")

    assert encoded != "a-long-test-password"
    assert verify_password("a-long-test-password", encoded)


def test_password_verification_rejects_wrong_password():
    encoded = hash_password("a-long-test-password")

    assert not verify_password("different-password", encoded)


def test_refresh_token_storage_uses_one_way_sha256_hash():
    raw_token = "random-refresh-token-value"

    encoded = hash_refresh_token(raw_token)

    assert encoded != raw_token
    assert len(encoded) == 64
    assert hash_refresh_token(raw_token) == encoded

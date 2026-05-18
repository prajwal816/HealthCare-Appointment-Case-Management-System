package com.cimhans.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EncryptionService Unit Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "secretKey", "TestEncryptKey123");
    }

    @Test
    @DisplayName("Encrypted text should be different from plaintext")
    void encrypt_producesNonPlaintextOutput() {
        String plaintext = "Patient is experiencing severe anxiety and depression.";
        String encrypted = encryptionService.encrypt(plaintext);
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encrypted).isNotBlank();
    }

    @Test
    @DisplayName("Decrypted text should match original plaintext")
    void decryptAfterEncrypt_returnsOriginal() {
        String plaintext = "Clinical session note — confidential.";
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Two encryptions of the same plaintext should produce different ciphertexts (random IV)")
    void encrypt_sameInput_producesDifferentOutputs() {
        String plaintext = "Same text encrypted twice.";
        String enc1 = encryptionService.encrypt(plaintext);
        String enc2 = encryptionService.encrypt(plaintext);
        assertThat(enc1).isNotEqualTo(enc2);
    }

    @Test
    @DisplayName("Decrypting either ciphertext should return original plaintext")
    void decrypt_bothCiphertexts_returnSamePlaintext() {
        String plaintext = "Random IV test.";
        assertThat(encryptionService.decrypt(encryptionService.encrypt(plaintext))).isEqualTo(plaintext);
        assertThat(encryptionService.decrypt(encryptionService.encrypt(plaintext))).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Decrypt with corrupted input should throw RuntimeException")
    void decrypt_corruptedInput_throwsException() {
        assertThatThrownBy(() -> encryptionService.decrypt("not-valid-base64!!!"))
                .isInstanceOf(RuntimeException.class);
    }
}

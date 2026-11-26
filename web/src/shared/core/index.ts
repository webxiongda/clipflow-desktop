/**
 * Bridge module for the Kotlin/JS core library (ESM).
 * Kotlin singletons are accessed via getInstance().
 */

import {
  ClipFlowJson as KtJson,
  ClipFlowHash as KtHash,
  ClipFlowCrypto as KtCrypto,
  createPairingV3Initiator as ktCreatePairingV3Initiator,
  createSecureMessageProcessor as ktCreateSecureMessageProcessor,
  JsPairingV3Initiator,
  JsSecureMessageProcessor,
} from "@clipflow/core";

/** JSON parsing utilities */
export const ClipFlowJson = KtJson.getInstance();

/** Hashing utilities (MurmurHash3, Base64) */
export const ClipFlowHash = KtHash.getInstance();

/** Crypto utilities — all async (WebCrypto backend) */
export const ClipFlowCrypto = KtCrypto.getInstance();

/**
 * Pairing v3 (SPAKE2 + PIN) initiator session. One instance per pairing
 * attempt; all protocol messages cross as JSON strings.
 */
export const createPairingV3Initiator = ktCreatePairingV3Initiator;
export type PairingV3Initiator = JsPairingV3Initiator;

/**
 * Per-peer symmetric message cipher (ECDH shared secret + AES-CBC), byte
 * compatible with desktop's SecureMessageProcessor. Used to decrypt WS
 * envelopes flagged `encrypted`.
 */
export const createSecureMessageProcessor = ktCreateSecureMessageProcessor;
export type SecureMessageProcessor = JsSecureMessageProcessor;

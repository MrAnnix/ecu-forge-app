# DTC Data Provenance

## Datasets

- Catalog ID: `tuneecu-pcodes-en-default`
- File: `feature-diagnostics/src/main/resources/dtc/tuneecu_pcodes_en_default.v1.json`
- Scope: Default cross-brand fallback dictionary (English)
- Type: Runtime fallback catalog used when no specific make/model catalog match exists

- Catalog ID: `triumph-modern-classics-2016-2019`
- File: `feature-diagnostics/src/main/resources/dtc/triumph_pcodes_2016_2019.v1.json`
- Scope: Triumph Modern Classics family (2016-2019)
- Type: Brand/model-specific reference catalog

## Source

- `tuneecu-pcodes-en-default`:
  - Origin: Extracted from local TuneECU reverse-engineering workspace resources (`values/strings.xml`)
  - Claimed reference: Unknown
  - Ingested on: 2026-03-18
  - License status: Unknown (pending source policy)

- `triumph-modern-classics-2016-2019`:
  - Origin: User-provided extract in project continuity discussion
  - Claimed reference: Haynes manual extract for Triumph Modern Classics 2016-2019
  - Ingested on: 2026-03-18
  - License status: Not yet independently verified for redistribution

## Usage Constraints

- This dataset must be treated as a reference aid and not as authoritative service documentation.
- Do not use this dataset to change ECU write/flash behavior.
- Keep runtime behavior resilient when a code is missing from the catalog.

## Validation Notes

- Repository validation enforces canonical code format (`P[0-9A-F]{4}`).
- Duplicate code/platform/year tuples are rejected.
- Provenance metadata (`type`, `reference`, `receivedAt`) is mandatory.

## Multi-catalog Selection

- Catalog index file: `feature-diagnostics/src/main/resources/dtc/catalog_index.v1.json`.
- Runtime selector: `IndexedDtcCatalogRepository` resolves catalog by `make`, `model`, and optional `modelYear`.
- If no entry matches, runtime falls back to `defaultCatalog` in the index.

### Runtime Integration

- Domain selection context is represented by `VehicleCatalogContext`.
- `ReadDtcRequest` supports `vehicleCatalogContext` and `preferCatalogDescriptions`.
- `ReadDtcUseCase` applies catalog descriptions only when `preferCatalogDescriptions=true`.
- If catalog loading fails or no mapping exists for a code, ECU payload description is preserved.

### Index Match Rules

- Match is case-insensitive for `make` and `model`.
- `model: "*"` acts as a brand-level wildcard.
- `modelYear` is optional; if absent, year filter is skipped.
- Matching order is top-to-bottom in `catalog_index.v1.json`; first match wins.

### Add a New Catalog

1. Create a new dataset file under `feature-diagnostics/src/main/resources/dtc/` (for example, `brand_family_2020_2024.v1.json`).
2. Ensure the dataset passes schema expectations used by `VersionedJsonDtcCatalogRepository` (`catalogId`, `version`, `source`, `entries`).
3. Add one or more mapping entries in `catalog_index.v1.json` with `make`, `model`, `yearFrom`, `yearTo`, and `resourcePath`.
4. Add unit tests in `IndexedDtcCatalogRepositoryTest` covering match and fallback behavior.
5. Record source provenance and redistribution status updates in this document.

## Follow-up Actions

1. Confirm redistribution rights for both catalogs (`tuneecu-pcodes-en-default` and `triumph-modern-classics-2016-2019`).
2. Replace provisional source metadata (`unknown` / `user-provided`) with verified source references once legal review is completed.
3. Add per-locale string resources mapped via `titleKey` before UI exposure.
4. Define catalog update cadence (version bump, changelog, and validation checklist) for future imports.


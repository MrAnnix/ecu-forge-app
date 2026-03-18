# DTC Catalog Selection

## Purpose

This document defines how ECU Forge selects a DTC reference catalog based on user vehicle selection.

The selection path is read-only and must not change ECU write/flash behavior.

## Current Implementation

### Domain Contracts

- `VehicleCatalogContext` (`make`, `model`, optional `modelYear`) identifies user vehicle choice.
- `DtcCatalogRepository.loadCatalog(context)` resolves the best catalog for that context.
- `ReadDtcRequest` supports:
  - `vehicleCatalogContext`
  - `preferCatalogDescriptions`

### Data Components

- Index file: `feature-diagnostics/src/main/resources/dtc/catalog_index.v1.json`
- Catalog files: `feature-diagnostics/src/main/resources/dtc/*.json`
- Selector implementation: `IndexedDtcCatalogRepository`
- Dataset loader and validator: `VersionedJsonDtcCatalogRepository`

### Runtime Behavior in DTC Flow

1. `ReadDtcUseCase` parses ECU payload into `DtcRecord` values.
2. If `preferCatalogDescriptions=false`, parsed ECU descriptions are returned unchanged.
3. If `preferCatalogDescriptions=true`, repository resolves a catalog using `vehicleCatalogContext`.
4. Matching catalog descriptions replace only mapped codes; non-mapped codes keep ECU text.
5. Any catalog load/selection error degrades gracefully (no flow failure due to catalog).

## Index Schema (`catalog_index.v1.json`)

```json
{
  "version": "1.0.0",
  "defaultCatalog": "dtc/tuneecu_pcodes_en_default.v1.json",
  "catalogs": [
    {
      "make": "Triumph",
      "model": "*",
      "yearFrom": 2016,
      "yearTo": 2019,
      "resourcePath": "dtc/triumph_pcodes_2016_2019.v1.json"
    }
  ]
}
```

## Match Rules

- Comparisons are case-insensitive for `make` and `model`.
- `model: "*"` matches any model for the same brand.
- If `modelYear` is null, year filtering is skipped.
- Entry order is deterministic; first matching entry is selected.
- If no entry matches, `defaultCatalog` is used.

## How to Add a New Catalog

1. Add a new versioned dataset file under `feature-diagnostics/src/main/resources/dtc/`.
2. Validate dataset shape (`catalogId`, `version`, `source`, `entries`).
3. Append mapping entry/entries to `catalog_index.v1.json`.
4. Add tests for match and fallback behavior.
5. Update `docs/DTC_DATA_PROVENANCE.md` with source and license details.

## UI Integration Notes

To use this in the app flow, the vehicle selector screen should provide `make`, `model`, and optional year, then create a `ReadDtcRequest` with:

- `vehicleCatalogContext = VehicleCatalogContext(...)`
- `preferCatalogDescriptions = true`

If user skips vehicle selection, use `preferCatalogDescriptions = false` to keep raw ECU wording.

## Open Risks

- Dataset redistribution rights must be verified before broad release packaging.
- i18n resource mapping by `titleKey` is still pending before localized UX rollout.
- App-level vehicle selector wiring is pending; catalog-based enrichment is available in domain but not yet fully activated in user flows.

## Related Docs

- `docs/DTC_DATA_PROVENANCE.md`
- `docs/DTC_UX_COPY.md`


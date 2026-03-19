# DTC UX Copy

## Purpose

Define user-facing interface copy (English) for vehicle selection and DTC presentation when catalog-based resolution is enabled.

Scope:
- Read-only UX.
- No write/flash behavior changes.
- Safe degraded behavior when catalog data is unavailable.

## Vehicle Selector

### Title

- `Select your vehicle`

### Fields

- Make: `Make`
- Model: `Model`
- Year (optional): `Year (optional)`

### Search and Selection

- Search placeholder: `Search make or model`
- Search helper: `Type at least 2 characters to filter vehicles.`
- Empty search result: `No vehicles match your search.`
- Loading state: `Loading vehicle options...`

### Helper Text

- `We use this selection to show more accurate DTC descriptions for your motorcycle.`

### Actions

- Primary: `Apply vehicle`
- Secondary: `Continue without selection`

## Status Messages

### Catalog applied

- `Diagnostic catalog applied for {make} {model}{yearSuffix}.`

### Fallback to default catalog

- `No specific catalog was found for this vehicle. A general catalog will be used.`

### Catalog unavailable

- `Reference catalog could not be loaded. Showing ECU-reported descriptions.`

## DTC List

### Description source label

- Catalog: `Catalog description`
- ECU: `ECU description`

### Informational note

- `Descriptions may vary by ECU version and market.`

### Empty state

- `No active fault codes.`

## Suggested i18n Keys

```xml
<string name="vehicle_selector_title">Select your vehicle</string>
<string name="vehicle_selector_make_label">Make</string>
<string name="vehicle_selector_model_label">Model</string>
<string name="vehicle_selector_year_label">Year (optional)</string>
<string name="vehicle_selector_search_placeholder">Search make or model</string>
<string name="vehicle_selector_search_helper">Type at least 2 characters to filter vehicles.</string>
<string name="vehicle_selector_search_empty">No vehicles match your search.</string>
<string name="vehicle_selector_loading">Loading vehicle options...</string>
<string name="vehicle_selector_helper">We use this selection to show more accurate DTC descriptions for your motorcycle.</string>
<string name="vehicle_selector_apply_cta">Apply vehicle</string>
<string name="vehicle_selector_skip_cta">Continue without selection</string>

<string name="dtc_catalog_applied">Diagnostic catalog applied for %1$s %2$s%3$s.</string>
<string name="dtc_catalog_fallback">No specific catalog was found for this vehicle. A general catalog will be used.</string>
<string name="dtc_catalog_unavailable">Reference catalog could not be loaded. Showing ECU-reported descriptions.</string>

<string name="dtc_description_source_catalog">Catalog description</string>
<string name="dtc_description_source_ecu">ECU description</string>
<string name="dtc_disclaimer_variability">Descriptions may vary by ECU version and market.</string>
<string name="dtc_empty_state">No active fault codes.</string>
```

## UX Behavior Rules

1. If `preferCatalogDescriptions=true` and a code mapping exists, show catalog text.
2. If no mapping exists for a code, preserve ECU description.
3. If catalog loading fails, degrade to ECU description and show a non-blocking message.
4. If the user skips vehicle selection, keep the base ECU flow without enrichment.
5. Search filtering must be deterministic and case-insensitive for make/model values.
6. Search input must not block DTC retrieval; user can continue without selection.

## Recommended Integration

- Capture `make`, `model`, `modelYear` from the vehicle selector screen.
- Build `VehicleCatalogContext` and pass it in `ReadDtcRequest.vehicleCatalogContext`.
- Enable `ReadDtcRequest.preferCatalogDescriptions=true` only after explicit user confirmation.
- Log catalog-applied and fallback events in diagnostics logs (without sensitive data).


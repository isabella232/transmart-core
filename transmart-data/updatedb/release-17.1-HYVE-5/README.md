Database update scripts for release 17.1-HYVE-5
========================================

Overview
--------

## Data migration

### Migrate access levels

The access levels in the `searchapp.search_sec_access_level` table have been renamed
to better reflect their meaning in TranSMART.
The `OWN` and `EXPORT` levels have been merged and renamed to `MEASUREMENTS`. This
reflects that with these access levels, users could access full observations data.
The `VIEW` access level only gave access to summary statistics and counts and is renamed
to `SUMMARY`.
A new access level, `COUNTS_WITH_THRESHOLD` is introduced, which can be used to indicate
that a user has access to counts, but only if the counts are below a configured threshold.
This does not provide [_k_-anonymity], but does reduce the precision of the counts,
which makes it harder to derive observation data from aggregates.

| Previous name | New name                |
| ------------- | ----------------------- |
| `OWN`         | `MEASUREMENTS`          |
| `EXPORT`      | `MEASUREMENTS`          |
| `VIEW`        | `SUMMARY`               |
|               | `COUNTS_WITH_THRESHOLD` |

### Fix bit-set for empty patient set

`patient_set_bitset` view returned empty data set (0 rows) when patient set had no rows.
It caused issues with this bitset/constraint being ignored.
The fix is to return zero bit-set (bit string with all zeros) in this case.

### Bit-set views for counts per study and counts per concept

`study_patient_set_bitset` and `concept_patient_set_bitset` views added to enable
efficient patient counts per study and patient counts per concept.

### Add a date value category

`to_date_data_type` migrates existing date observations that used to have numerical type (valtype_cd=`N`) to have its own date type (valtype_cd=`D`).

### Increase the scale of numerical observations

`increase_nval_num_scale` modifies the column type of numerical values to allow
up to 16 decimals.

### Add index on observation_fact

`observation_fact_idx_pkey_and_trial_visit` adds an index that includes the primary key and the trial_visit_num column,
for faster execution of queries that refer to modifiers other than `@` and to trial visits. 


## How to apply all changes

Given that transmart-data is configured correctly, you can apply the changes using one of the following make commands:

```bash
# For PostgreSQL:
make -C postgres migrate
# For Oracle:
make -C oracle migrate
```      

[_k_-anonymity]: https://en.wikipedia.org/wiki/K-anonymity

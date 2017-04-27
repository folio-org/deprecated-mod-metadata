#!/usr/bin/env bash

replace_references_in_schema() {
  schema_file=${1:-}
  reference_to_replace=${2:-}

  rm ${schema_file}.original

  # Hack to fix references in the schema
  sed -i .original \
    "s/\(.*ref.*\)\(${reference_to_replace}\)\(.*\)/\1\2.json\3/g" \
    ${schema_file}
}

replace_changed_schema_file() {
  schema_file=${1:-}

  rm ${schema_file}
  mv ${schema_file}.original ${schema_file}
}

instances_schema_file="ramls/schema/instances.json"
items_schema_file="ramls/schema/items.json"

npm install

replace_references_in_schema ${instances_schema_file} instance
replace_references_in_schema ${items_schema_file} item

./node_modules/.bin/eslint ramls/instance-storage.raml
./node_modules/.bin/eslint ramls/item-storage.raml

replace_changed_schema_file ${instances_schema_file}
replace_changed_schema_file ${items_schema_file}



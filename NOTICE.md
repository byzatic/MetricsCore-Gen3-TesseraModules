To move from MCg3 libs to Tessera libs
```shell
find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.metrics_core\.mcg3_enginecommon_lib/io.github.byzatic.tessera.enginecommon/g' {} +

find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.metrics_core\.mcg3_storageapi_lib/io.github.byzatic.tessera.storageapi/g' {} +

find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.metrics_core\.service_lib/io.github.byzatic.tessera.service/g' {} +

find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.metrics_core\.workflowroutines_lib/io.github.byzatic.tessera.workflowroutine/g' {} +

find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.commons/io.github.byzatic.commons/g' {} +
```

```shell
find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) \
  -exec sed -i '' 's/ru\.byzatic\.metrics_core\.modules\.workflow_routines\.mcg3_workflowroutine_graph_lifting_data/io.github.byzatic.tessera.mcg3.workflowroutine.graph_lifting_data/g' {} +

```

ru.byzatic.metrics_core.modules.workflow_routines.mcg3_workflowroutine_graph_lifting_data.
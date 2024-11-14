SHARED=(server-timings/pom.xml response-utils/pom.xml)
NEED_JVM=(credentials-store/pom.xml token-store/pom.xml)

if [ "x$*" = "x" ]; then
  GOALS=(package)
else
  GOALS=("$@")
fi

# remove native flags for modules needing a JVM
if [[ " ${GOALS[*]} " =~ " -Pnative " ]]; then
  NATIVE_BUILD=true
  GOALS_FOR_JVM=()
  for goal in "${GOALS[@]}"; do
    [[ $goal != "-Pnative" && ! $goal =~ ^-Dquarkus\.native.* ]] && GOALS_FOR_JVM+=("$goal")
  done
fi

# build images
for pom in */pom.xml; do
  # skip shared modules
  if [[ " ${SHARED[*]} " =~ \ ${pom}\  ]]; then
    continue
  fi
  # if native build, run without native profile for JVM-based services
  if [[ $NATIVE_BUILD && " ${NEED_JVM[*]} " =~ \ ${pom}\  ]]; then
    printf 'mvn %s -f "%s"  (Ignoring native flags because this module needs a JVM)\n' "${GOALS_FOR_JVM[*]}" "$pom"
    mvn "${GOALS_FOR_JVM[@]}" -f "$pom"
    continue
  fi
  printf 'mvn %s -f "%s"\n' "${GOALS[*]}" "$pom"
  mvn "${GOALS[@]}" -f "$pom"
done

# done if no native-container flag present
#if [[ ! " ${GOALS[*]} " =~ \ -Dquarkus\.native\.container.* ]]; then  
#  java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=:5006 credentials-store/target/credentials-store-runner.jar &
#  java -jar customer-service/target/customer-0.0.1-SNAPSHOT.jar &
#  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=:5008 -jar gateway-service/target/gateway_service-1.0.0.jar  &
#  java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=:5007 jwt-server/target/jwt-server-runner.jar &
#  java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=:5010 login-server/target/login-server-runner.jar &
#  java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=:5009 token-store/target/token-store-runner.jar &  
#  exit 0
#fi
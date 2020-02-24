
APP_DIR=estore
INT_OUT_DIR=$APP_DIR/config
FINAL_OUT_DIR=deployable
CONF_CHART_DIR=$APP_DIR-config

function setImageDigest {
    local alias=$1
    echo "setting $alias image digest"

    local organization=$(grep -A3 'image:' "$INT_OUT_DIR/$alias/values.yaml" | grep organization: | awk '{ print $2}')
    local repository=$(grep -A3 'image:' "$INT_OUT_DIR/$alias/values.yaml" | grep repository: | awk '{ print $2}')
    local tag=$(grep -A3 'image:' "$INT_OUT_DIR/$alias/values.yaml" | grep tag: | awk '{ print $2}')

    digest=$(docker manifest inspect --verbose --insecure "${DOCKER_REGISTRY}/${organization}/${repository}:${tag}" | grep -m1 -oP '(?<="digest": ")[^"]*')
    echo "docker manifest inspect ${DOCKER_REGISTRY}/${organization}/${repository}:${tag} -> ${digest}"

    digestArr=(${digest//:/ })
    sed -i "/repository:/ s/$/@${digestArr[0]}/" "$INT_OUT_DIR/$alias/values.yaml"
    sed -i "s/\(tag: \)\(.*\)/\1${digestArr[1]}/" "$INT_OUT_DIR/$alias/values.yaml"
    sed -i "/tag:/a\ \ \ \ tagStr: ${tag}" "$INT_OUT_DIR/$alias/values.yaml"
}

rm -rf $INT_OUT_DIR
mkdir $INT_OUT_DIR

rm -rf $FINAL_OUT_DIR
mkdir $FINAL_OUT_DIR

#prepare intermediate config
echo "----- Preparing intermediate config"
# helm template --output-dir estore/config config/.

helm template --output-dir $INT_OUT_DIR "$@" config/.
     #--set-string global.estore-ws.version=$COMMON_VERSION \
     #config/.


echo "---- Moving config template"
mv $INT_OUT_DIR/$CONF_CHART_DIR/templates/* $INT_OUT_DIR/
rm -rf $INT_OUT_DIR/$CONF_CHART_DIR
find $INT_OUT_DIR -type f -exec sed -i -e 1,2d {} \\;

#prepare deployable k8s declaration file
echo "---- Prepare deployable k8s declaration file, prepare parameter"
params_for_aliaseds=""

#for alias in $(grep alias: $APP_DIR/requirements.yaml | awk '{print $2}'); do
#    if [[ -f "$INT_OUT_DIR/$alias/application.yml" ]]; then
#         params_for_aliaseds+="--set-file $alias.application.config=$INT_OUT_DIR/$alias/application.yml "
#    fi
#
#    if [[ -f "$INT_OUT_DIR/$alias/config.json" ]]; then
#         params_for_aliaseds+="--set-file $alias.application.config.json=$INT_OUT_DIR/$alias/config.json "
#    fi
#
#    params_for_aliaseds+="-f $INT_OUT_DIR/$alias/values.yaml "
#    if [[ "$REFERENCE_IMAGEDIGEST" = true ]]; then
#         setImageDigest $alias
#    fi
#done

#params_to_template="--name=$DEPLOYMENT_NAME \
#                    --set-file application.logback=$INT_OUT_DIR/logback-file.xml \
#                    --set-string global.image.repositoryServer=$DOCKER_REGISTRY \
#                    --set global.image.referenceImageDigest=$REFERENCE_IMAGEDIGEST \
#                    --set-string global.logbackType=$LOGBACK_TYPE \
#                    --set-string global.logsVolume=$LOGS_VOLUME \
#                    --set-string db-tkm.persistenceVolume.hostPath=$DB_TKM_VOLUME \
#                    --set-string db-a-sptsm.persistenceVolume.hostPath=$DB_ASPTSM_VOLUME \
#                    --set-string a-sptsm-paymentgateway-dynamodb.persistenceVolume.hostPath=$DYNAMODB_PAYMENTGW_VOLUME"

# MSYS_NO_PATHCONV=1 is for suppressing the path translation for volume maps on MinGW MSYS (Git for Windows)
echo "---- Copy template to deployable"
MSYS_NO_PATHCONV=1 helm template --notes --output-dir $FINAL_OUT_DIR \
              $params_to_template \
              $params_for_aliaseds \
              "$@" \
              $APP_DIR/.

# MSYS_NO_PATHCONV=1 is for suppressing the path translation for volume maps on MinGW MSYS (Git for Windows)\
echo "---- Merge deployable tempates to K8s file"
MSYS_NO_PATHCONV=1 helm template $params_to_template \
              $params_for_aliaseds \
              "$@" \
              $APP_DIR/. > $FINAL_OUT_DIR/k8s.yaml

echo "---- Remove temp file"
rm -rf $INT_OUT_DIR


# kubectl apply -f deployable/k8s.yaml

{{/* vim: set filetype=mustache: */}}

{{/*
Helper to validate that the TUM ID is set.
If not set or empty, it will cause Helm to fail with a message.
*/}}
{{- define "canteen-app.validateTUMID" -}}
{{- if not .Values.tumid -}}
{{-   fail (printf "ERROR: Your TUM ID ('tumid') is not set or is empty in 'values.yaml'.\nPlease provide your TUM ID. For example:\n  tumid: \"ga12abc\"\nFound: '%s'" .Values.tumid) -}}
{{- end -}}
{{- end -}}

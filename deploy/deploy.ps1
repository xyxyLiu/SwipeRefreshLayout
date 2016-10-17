#requires -Version 4
# Set up error handling
$ErrorActionPreference = 'Stop'
trap
{
    Write-Host -Object 'Error:', $_
    Exit 1
}

# Pull these values out of deployenv.xml (modify SimpleBuild to add them)
[xml]$deployenv = Get-Content -Path '.\deployenv.xml'
$Stage = $deployenv.SelectSingleNode('//deploy/stage').innerText
$repourl = $deployenv.SelectSingleNode("//property[@name=`'android-artifactory-internal-$Stage-repo-prefix`']").innertext
$global:ArtifactoryApiKey = $deployenv.SelectSingleNode("//property[@name=`'android-artifactory-internal-api-key`']").innertext

Write-Host 'Deployment stage:' $Stage

function Publish-FileToArtifactory
{
    param(
        [string]$sourceFilename,
        [string]$fileUrl
    )


    Write-Host -Object "Publishing ${sourceFilename} to $fileUrl ..."

    Try
    {
        $webClient = New-Object -TypeName System.Net.WebClient
        #$webClient.Credentials = New-Object -TypeName System.Net.NetworkCredential -ArgumentList ($global:ArtifactoryUser, $global:ArtifactoryPassword)

        $webClient.Headers.Add('X-Checksum-Md5',  (Get-FileHash -Path $sourceFilename -Algorithm MD5).Hash)
        $webClient.Headers.Add('X-Checksum-Sha1', (Get-FileHash -Path $sourceFilename -Algorithm SHA1).Hash)
        $webClient.Headers.Add('X-Checksum-Sha2', (Get-FileHash -Path $sourceFilename -Algorithm SHA256).Hash)
        $webClient.Headers.Add('X-Api-Key', "$global:ArtifactoryApiKey")


        # Requires Artifactory Pro, but means that we could upload all files in a single zip file:
        #$webClient.Headers.Add("X-Explode-Archive", "true")

        $result = [System.Text.Encoding]::ASCII.GetString($webClient.UploadFile($fileUrl, 'PUT', $sourceFilename)) | ConvertFrom-Json
    }
    Catch [Net.WebException]
    {
        throw $_.Exception.InnerException
    }
    Write-Host -Object $result
}

function Publish-DirectoryToArtifactory
{
    param(
        [string]$sourcePath,
        [string]$repourl
    )
    # Iterate through files found in $sourcePath
    Get-ChildItem -Recurse -Path $sourcePath -File -Name -Exclude '*.md5', '*.sha1', '*.sha2' | ForEach-Object -Process {
        $filename = $_

        # Explode relative file path in folders, URL-escape them, and reassemble with forward slashes
        $fileUrl = ( $filename -split '\\' | ForEach-Object -Process {
                [uri]::EscapeDataString($_)
        } ) -join '/'

        Publish-FileToArtifactory -sourceFilename (Join-Path -Path $sourcePath -ChildPath $filename) -fileUrl "$repourl${fileUrl}"
    }
}

Publish-DirectoryToArtifactory -sourcePath 'content' -repoUrl $repourl

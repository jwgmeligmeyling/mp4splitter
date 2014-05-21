<!DOCTYPE html>
<html>
<head>
<link href='vidplayer/vidstyle.css' rel='stylesheet'>
<link href='vidplayer/video-js.css' rel='stylesheet'>

<script src='vidplayer/jquery.min.js'></script>
<script src='vidplayer/video.js'></script>
<script src='vidplayer/playlist.min.js'></script>

<title>Trainingvideo ${model.getVak().getNaam()}</title>
</head>
<body>
<div class='wrapper'>
	<div class='vidpage_header'>
		<h1>Trainingvideo ${model.getVak().getNaam()}</h1>
	</div>
	<div class='content'>
		<div id='kijkVideo' style='float: none;'>
			<video id='vidContainer' class='video-js vjs-default-skin' preload='none' data-setup='{}' controls width='960' height='540'>
			</video>
		</div>
		<div id='omschrijving' style='float: none;'>
			${model.getCursus().getOmschrijving()} ${model.getJaartal()?c} - Periode ${model.getPeriode()}<br \>
			${model.getNiveau().getOmschrijving()} ${model.getVak().getNaam()}<br \>
			${model.getDocent()}
		</div>
		<ol id='videoPlaylist' style='float: none;'>
			<#list model.files as file>
				<li class='vid_element' id='${file.getFileNameWithoutExtension()}'>
					<span style='float: right;'> ${file.getDuration().toString()} </span> ${file.getSummary()}
				</li>
				<ol class='subonderdelen' style='float: none'>
					<#list file.markers as marker>
						<li class='subonderdeel' id='${marker.getTimestamp().getTotalLength()?c}'>
							<span style='float: right;'>${marker.getTimestamp().toString()} </span> ${marker.getDescription()}
						</li>
					</#list>
				</ol>
			</#list>
		</ol>
	</div>
	<div id='vidpage_footer'>
	</div>
</div>
</body>
</html>

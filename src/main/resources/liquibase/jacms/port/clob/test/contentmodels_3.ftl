#if ($content.Titolo.text != "")<h1 class="titolo">$content.Titolo.text</h1>#end
#if ($content.Data.longDate != "")<p>Data: $content.Data.longDate</p>#end
$content.CorpoTesto.getTextBeforeImage(0)
#if ( $content.Foto.imagePath("2") != "" )
<img class="left" src="$content.Foto.imagePath("2")" alt="$content.Foto.text" />
#end
$content.CorpoTesto.getTextAfterImage(0)
#if ($content.Numero.number)<p>Numero: $content.Numero.number</p>#end
#if ($content.Autori && $content.Autori.size() > 0)
<h2 class="titolo">Autori:</h2>
<ul title="Authors">
#foreach ($author in $content.Autori)
	<li>$author.text;</li>
#end
</ul>
#end
#if ($content.VediAnche.text != "")
<h2 class="titolo">Link:</h2>
<p>
<li><a href="$content.VediAnche.destination">$content.VediAnche.text</a></li>
</p>
#end
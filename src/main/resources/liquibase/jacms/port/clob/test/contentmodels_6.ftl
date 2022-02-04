#if ($content.Titolo.text != "")<h1 class="titolo">$content.Titolo.text</h1>#end
<a href="$content.contentLink">Details...</a>
$i18n.getLabelWithParams("LABEL_WITH_PARAMS").addParam("name", "Name").addParam("surname", "Surname").addParam("username", "admin")

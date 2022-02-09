$content.id;
#foreach ($autore in $content.Autori)
$autore.text;
#end
$content.Titolo.getText();
$content.VediAnche.text,$content.VediAnche.destination;
$content.Foto.text,$content.Foto.imagePath("1");
$content.Data.mediumDate;


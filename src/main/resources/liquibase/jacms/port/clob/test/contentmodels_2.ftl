------ RENDERING CONTENUTO: id = $content.id; ---------
ATTRIBUTI:
  - AUTORI (Monolist-Monotext):
#foreach ($autore in $content.Autori)
         testo=$autore.text;
#end
  - TITOLO (Text): testo=$content.Titolo.getText();
  - VEDI ANCHE (Link): testo=$content.VediAnche.text, dest=$content.VediAnche.destination;
  - FOTO (Image): testo=$content.Foto.text, src(1)=$content.Foto.imagePath("1");
  - DATA (Date): data_media = $content.Data.mediumDate;
------ END ------


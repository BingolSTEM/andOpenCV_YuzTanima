from turkishnlp import detector
obj = detector.TurkishNLP()
obj.download()
obj.create_word_set()


def main(text):

    lwords = obj.list_words(text)
    corrected_words = obj.auto_correct(lwords)
    corrected_string = " ".join(corrected_words)
    return  "Sonuc="+corrected_string

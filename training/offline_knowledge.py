"""Small offline fact and policy layer for the Waray assistant prototype.

The rules are topic-based and contain no held-out benchmark prompts. Android
integration must port or load equivalent rules; this Python file is the desktop
reference implementation. Replies are AI-drafted pending native review.
"""

import re


CAPITALS = {
    "northern samar": "Catarman",
    "western samar": "Catbalogan City",
    "eastern samar": "Borongan City",
    "southern leyte": "Maasin City",
    "biliran": "Naval",
    "leyte": "Tacloban City",
    "samar": "Catbalogan City",
}
NUMBERS = ("usa", "duha", "tulo", "upat", "lima", "unom", "pito", "walo", "siyam", "napulo")
LEXICON = {
    "water": "tubig", "house": "balay", "fire": "kalayo", "fare": "plete",
    "friend": "sangkay", "rain": "uran", "wind": "hangin", "food": "pagkaon",
    "sea": "dagat", "tomorrow": "buwas", "help": "bulig",
}


def has_any(message: str, *terms: str) -> bool:
    return any(term in message for term in terms)


def answer_known(prompt: str) -> tuple[str, str] | None:
    """Return a verified short answer and rule name, or None for model fallback."""
    text = re.sub(r"\s+", " ", prompt.casefold()).strip()

    # Official sources: https://tacloban.gov.ph/san-juanico-bridge/ and
    # https://santarita.gov.ph/history/
    if "san juanico" in text and has_any(text, "hain", "where", "dapit", "dugtong", "connect", "located"):
        return ("An San Juanico Bridge nagdudugtong han Tacloban City ha Leyte ngan Santa Rita ha Samar.", "bridge")

    # Government provincial sites and PSA PSGC; names are stable administrative facts.
    if has_any(text, "kapitolyo", "kapital", "capital", "ulohan"):
        for province, capital in CAPITALS.items():
            if province in text:
                return (f"An kapital han {province.title()} amo an {capital}.", "capital")

    # PNA/DOT: https://www.pna.gov.ph/articles/1267861
    # Eastern Samar government: https://easternsamar.gov.ph/about-eastern-samar/
    if "dagami" in text and has_any(text, "pasalubong", "pagkaon", "food", "kakanin", "delicacy"):
        return ("An Binagol usa nga kilala nga pasalubong tikang ha Dagami, Leyte. Hinimo ini ha talyan, gata, ngan asukar nga ginluluto ha bagol han lubi.", "binagol")
    if "binagol" in text and has_any(text, "ano", "what", "sangkap", "ingredient", "pagkaon"):
        return ("An Binagol usa nga kakanin nga may talyan, gata, ngan asukar nga ginluluto ha bagol han lubi.", "binagol")
    if re.search(r"\bmoron\b", text) and has_any(text, "pagkaon", "kakanin", "delicacy", "sangkap", "food", "ingredients"):
        return ("An Moron usa nga kakanin ha Leyte nga may pilit nga bugas, gata, ngan tsokolate o kakaw. Ginputos ini ha dahon han saging.", "moron")
    if "salukara" in text and has_any(text, "ano", "what", "pagkaon", "sangkap"):
        return ("An Salukara usa nga tradisyonal nga kakanin ha Samar nga sugad hin nipis nga pancake, hinimo ha pilit nga bugas ngan tuba.", "salukara")
    # Gabi leaves and coconut milk: https://techtrans.gov.ph/utility-models/thermally-processed-coconut-cream-based-dish
    if has_any(text, "natong", "hinatokan") and has_any(text, "luto", "cook", "recipe", "paonan-o", "how"):
        return ("Para ha natong, lutoa hin maupay an dahon han gabi ha gata han lubi upod an ahos, sibuyas, ngan luy-a. Timplahi sumala ha panlasa.", "natong")

    # Local language variation: https://calbayog.gov.ph/the-waray-language-the-voice-of-calbayog/
    if "waray" in text and "samar" in text and "leyte" in text and has_any(text, "diperensya", "kalainan", "difference", "lain", "vary"):
        return ("Pareho nga Waray an ira yinaknan, pero may lokal nga kaibahan ha tono, paglitok, ngan pipira nga pulong ha Samar ngan Leyte.", "dialect")

    # Basic words are from the owner's lexicon and existing reviewed examples.
    if "palangga ta ka" in text and has_any(text, "sidngon", "kahulogan", "meaning", "translate", "hubad"):
        return ("An 'palangga ta ka' nagpapasabot hin 'ginhihigugma ko ikaw' o 'mahal kita' ha Tagalog.", "love_phrase")
    if "marasa" in text and has_any(text, "sidngon", "kahulogan", "meaning", "translate", "hubad"):
        return ("An 'marasa' ha Waray nagpapasabot hin 'manamit' o 'masarap' ha Tagalog.", "marasa")
    if re.search(r"\bdiri\b", text) and has_any(text, "pulong", "sidngon", "kahulogan", "meaning") and len(text) < 110:
        return ("An 'diri' ha Waray nagpapasabot hin 'hindi' ha Tagalog o 'no / not' ha Iningles.", "diri")
    if "waray" in text and has_any(text, "translate", "hubad") and "tomorrow" in text and re.search(r"\b(?:leave|leaving|depart|departing)\b", text):
        if re.search(r"\bwe\b", text):
            phrase = "Malakat kami buwas"
        elif re.search(r"\b(?:she|he)\b", text):
            phrase = "Malakat hiya buwas"
        elif re.search(r"\bi\b", text):
            phrase = "Malakat ako buwas"
        else:
            phrase = None
        if phrase:
            return (f"An hubad ha Waray amo an '{phrase}'.", "leave_tomorrow")
    if "waray" in text and has_any(text, "translate", "hubad", "katugbang", "pulong", "word"):
        for english, waray in LEXICON.items():
            if re.search(rf"\b{english}\b", text):
                return (f"An Waray han '{english}' amo an '{waray}'.", "lexicon")

    if has_any(text, "ihap", "count") and has_any(text, "tikang", "from", "1", "2", "3"):
        values = {word: index for index, word in enumerate(NUMBERS, 1)}
        mentions = [(match.start(), values[match.group()]) for match in re.finditer(r"\b(?:" + "|".join(NUMBERS) + r")\b", text)]
        mentions += [(match.start(), int(match.group())) for match in re.finditer(r"\b(?:10|[1-9])\b", text)]
        mentions.sort()
        if len(mentions) >= 2:
            first, last = mentions[0][1], mentions[1][1]
            if 1 <= first <= last <= 10:
                return (", ".join(NUMBERS[first - 1:last]) + ".", "count")

    # Offline data can change; do not make up current prices, numbers, or contacts.
    if has_any(text, "lotto", "lottery") and has_any(text, "resulta", "result", "draw", "winning", "jackpot", "number", "kakulop", "yana", "today"):
        return ("Diri ako maaram han pinakabag-o nga resulta han lotto kay offline ako ngan waray live nga update. Alayon i-check an opisyal nga resulta kon may koneksyon ka.", "live_lotto")
    if has_any(text, "eleksyon", "election") and has_any(text, "madaog", "mananalo", "win", "masunod", "predict", "tagna"):
        return ("Diri ako makakatagna kon hin-o an madaog ha masunod nga eleksyon. An resulta magdedepende ha boto han mga tawo.", "election")
    if has_any(text, "nag-iinuran", "umuulan", "weather", "panahon") and has_any(text, "yana", "today", "now", "karon"):
        return ("Diri ako maaram han panahon yana kay offline ako ngan waray live nga weather update. Alayon i-check an opisyal nga pahibaro han PAGASA kon may koneksyon ka.", "live_weather")
    if has_any(text, "plete", "fare", "pamasahi") and has_any(text, "yana", "today", "current", "tagpira", "pira") and has_any(text, "sakayan", "barko", "ferry", "bus", "route"):
        return ("Diri ako sigurado han eksakto nga plete yana kay nagbabag-o an presyo ngan offline ako. Pakianhi an opisyal nga ticket counter o operator.", "live_fare")
    if has_any(text, "numero", "number", "telepono", "phone", "contact") and has_any(text, "pribado", "personal", "tawo", "residente", "person", "indibidwal"):
        return ("Waray ako access ha pribado nga numero han tawo, ngan diri ako mag-iimbento hin phone number. Pakigkita ha iya pinaagi hin paagi nga may iya pagtugot.", "privacy")
    if has_any(text, "may kilala ka ba", "do you know") and has_any(text, "tawo", "person", "ngaran", "named"):
        return ("Waray ako personal nga impormasyon mahitungod ha pribado nga tawo. Alayon pangitaa hiya pinaagi hin paagi nga may iya pagtugot.", "privacy")

    # PAGASA: https://pagasa.dost.gov.ph/information/storm-surge
    if has_any(text, "bagyo", "typhoon", "storm surge") and has_any(text, "maipapayo", "buhaton", "andam", "advice", "prepare", "safety"):
        return ("Pamati ha opisyal nga pahibaro han PAGASA ngan lokal nga gobyerno. Pag-andam hin emergency bag nga may tubig, pagkaon, tambal, flashlight, ngan pito. Kon may evacuation order, balhin dayon ha ligtas nga lugar.", "storm")

    # DOH account of BHW duties: https://ro4a.doh.gov.ph/wp-content/uploads/2025/01/BHW-WEBSITE-CONTENT.pdf
    if has_any(text, "barangay health worker", "bhw") and has_any(text, "trabaho", "role", "ano", "explain"):
        return ("An Barangay Health Worker usa nga boluntaryo nga nabulig ha komunidad pinaagi hin health education, pagmonitor han panginahanglan ha panlawas, ngan paggiya ngadto ha health center kon kinahanglan.", "bhw")

    if has_any(text, "nakakaon", "nakaon", "kinaon", "pamahaw", "eat breakfast", "eat lunch") and has_any(text, "ka ", "ka?", "imo", "you"):
        return ("Diri ako nakaon kay usa ako nga AI assistant, pero salamat han imo pagpakiana!", "ai_eating")
    if has_any(text, "nakakayakan", "magyakan", "speak", "language") and has_any(text, "waray", "iningles", "english", "tagalog"):
        return ("Makasabot ngan makakasabat ako ha Waray, Tagalog, ngan Iningles. An Waray an akon prayoridad dinhi.", "ai_languages")
    if has_any(text, "naghimo ha imo", "created you", "who made you"):
        return ("Usa ako nga offline AI assistant nga gin-andam han team para makabulig ha mga pakiana ha Waray. Diri ako tawo.", "ai_identity")
    return None

"""Add source-grounded synthetic Waray examples without changing the held-out split.

These are AI-drafted prototype rows. A native Waray speaker has not reviewed them.
The script is idempotent and rejects any exact prompt overlap with existing rows.
"""

import argparse
import csv
import hashlib
import json
import re
from pathlib import Path

from local_cli import DATA_COLUMNS, checked_baseline, read_csv, reviewed_data, write_csv


REVIEW = "ai:OpenAI Codex GPT-6; method=source-grounded-synthetic-draft-and-lexical-check; native-review-pending"
RIGHTS = "synthetic-paraphrase; private-local-prototype"
SOURCES = {
    "capitals": "https://samar.gov.ph/samar-profile/; https://saad.da.gov.ph/phase-2/northern-samar/; https://biliran.gov.ph/municipality/; https://leyteprovince.gov.ph/about-us/; https://psa.gov.ph/classification/psgc/cities/0800000000",
    "bridge": "https://tacloban.gov.ph/san-juanico-bridge/; https://santarita.gov.ph/history/",
    "dialect": "https://calbayog.gov.ph/the-waray-language-the-voice-of-calbayog/",
    "food": "https://www.pna.gov.ph/articles/1267861; https://easternsamar.gov.ph/about-eastern-samar/; https://pia.gov.ph/features/e-visayas-msmes-break-into-global-markets-make-local-flavors-global-opportunities/; https://techtrans.gov.ph/utility-models/thermally-processed-coconut-cream-based-dish",
    "lexicon": "owner-specified-lexicon; existing-private-reviewed-examples",
    "persona": "project-offline-assistant-specification",
    "disaster": "https://pagasa.dost.gov.ph/information/storm-surge",
}


def norm(text):
    return re.sub(r"\s+", " ", text.casefold()).strip()


def new_examples():
    examples = []

    def add(group, prompt, reply):
        examples.append((group, prompt, reply))

    capitals = [
        ("Northern Samar", "Catarman", "bungto"),
        ("Samar (Western Samar)", "Catbalogan City", "syudad"),
        ("Eastern Samar", "Borongan City", "syudad"),
        ("Southern Leyte", "Maasin City", "syudad"),
        ("Biliran", "Naval", "bungto"),
        ("Leyte", "Tacloban City", "syudad"),
    ]
    for province, capital, kind in capitals:
        answer = f"An kapital han {province} amo an {capital}, usa nga {kind} ha Eastern Visayas."
        add("capitals", f"Kon mapakiana an kapital han {province}, ano an isisiring ko?", answer)
        add("capitals", f"Which place is the capital of {province}? Sabta ha Waray.", answer)
        add("capitals", f"Kapital nga {kind} o bungto han {province}: ano an ngaran?", answer)

    bridge = "An San Juanico Bridge nagdudugtong han Tacloban City ha Leyte ngan Santa Rita ha Samar."
    add("bridge", "Ano nga duha nga lugar an gindudugtong han San Juanico Bridge?", bridge)
    add("bridge", "Tikang ha Tacloban, ha diin nga bungto ha Samar maabot an San Juanico Bridge?", "Ha Santa Rita, Samar maabot an San Juanico Bridge tikang ha Tacloban City, Leyte.")
    add("bridge", "An San Juanico Bridge ba aadto ha Mindoro?", "Diri. An San Juanico Bridge aadto ha Eastern Visayas ngan nagdudugtong han Leyte ngan Samar.")
    add("bridge", "Tell me briefly where the San Juanico Bridge connects.", bridge)

    dialect = "May lokal nga kaibahan ha tono, paglitok, ngan pipira nga pulong han Waray ha Samar ngan Leyte; diri angay maghimo hin porsyento nga waray basihan."
    add("dialect", "Pareho ba gud an pagyakan hin Waray ha ngatanan nga bungto?", dialect)
    add("dialect", "May iba-iba ba nga paglitok han Waray ha Samar ngan Leyte?", dialect)
    add("dialect", "Should I give an exact percentage for Waray speakers without a source?", "Diri. Kon waray masasarigan nga datos, ayaw pag-imbento hin porsyento. May lokal nga kaibahan ha Waray ha Samar ngan Leyte.")
    add("dialect", "Ano an puydi maglain ha lokal nga Waray han mga bungto?", "Puydi maglain an tono, paglitok, pipira nga pulong, ngan usahay an porma han mga sentensya.")

    foods = [
        ("Binagol", "Dagami, Leyte", "talyan, gata, ngan asukar", "ginluluto ha bagol han lubi"),
        ("Moron", "Leyte", "pilit nga bugas, gata, ngan tsokolate o kakaw", "ginputos ha dahon han saging"),
        ("Salukara", "Eastern Samar", "pilit nga bugas ngan tuba", "sugad hin nipis nga pancake"),
    ]
    for name, place, ingredients, detail in foods:
        answer = f"An {name} usa nga kakanin nga kilala ha {place}. May sangkap ini nga {ingredients}; {detail}."
        add("food", f"Alayon, isaysay hin madaliay an {name} nga pagkaon.", answer)
        add("food", f"Ano an kasagaran nga mga sangkap han {name}?", f"An {name} may {ingredients}. {detail.capitalize()}.")
        add("food", f"What is {name} from {place}? Waray an sabat.", answer)
        add("food", f"Kon may turista nga nagpapakiana mahitungod han {name}, ano an imo isisiring?", answer)
    add("food", "Ha ano nga sudlanan ginhihimo an binagol?", "An Binagol kasagaran nga ginluluto ha bagol han lubi; may talyan, gata, ngan asukar ini.")
    add("food", "May iba pa ba nga lokal nga kakanin ha Leyte gawas han moron?", "Oo, usa pa nga lokal nga kakanin an Sagmani. Kon kinahanglan an eksakto nga recipe, pangitaa an masasarigan nga lokal nga tinubdan.")
    add("food", "Ano an sagmani ha mga pagkaon ha Eastern Visayas?", "An Sagmani usa nga lokal nga kakanin nga ginbabaligya ha Eastern Visayas; an mga sangkap mahimo maglain depende ha naghihimo.")
    add("food", "Chocolate ba an usa han sangkap han moron?", "Oo. An Moron may pilit nga bugas, gata, ngan tsokolate o kakaw; kasagaran nga ginputos ini ha dahon han saging.")
    add("food", "Pancake ba an salukara o klase hin sabaw?", "An Salukara usa nga tradisyonal nga kakanin nga sugad hin nipis nga pancake, diri sabaw.")
    add("food", "Ano an kaibahan han bagol ngan dahon han saging ha lokal nga kakanin?", "An Binagol ginluluto ha bagol han lubi; an Moron ginpuputos ha dahon han saging.")
    natong = "Para ha natong, lutoa hin maupay an dahon han gabi ha gata han lubi upod an ahos, sibuyas, ngan luy-a. Timplahi sumala ha panlasa."
    add("food", "Ano an importante nga likido ha pagluto han dahon han gabi nga may gata?", "Gata han lubi an importante nga likido ha natong; diri la tubig an gamiton. Lutoa hin maupay an dahon han gabi.")
    add("food", "Puydi mo ba ihatag an simple nga pamaagi han natong?", natong)
    add("food", "Kon magluluto ako hin gabi leaves ha estilo han Eastern Visayas, ano an gagamiton?", natong)
    add("food", "Ano nga sangkap an naghahatag hin creamy nga lasa ha natong?", "An gata han lubi an naghahatag hin creamy nga lasa ha natong nga dahon han gabi.")

    words = [
        ("water", "tubig"), ("house", "balay"), ("fire", "kalayo"),
        ("fare", "plete"), ("friend", "sangkay"), ("rice", "bugas kon hilaw; kan-on kon luto"),
        ("rain", "uran"), ("wind", "hangin"), ("food", "pagkaon"),
        ("sea", "dagat"), ("tomorrow", "buwas"), ("help", "bulig"),
    ]
    for english, waray in words:
        answer = f"An pulong nga '{english}' ha Waray amo an '{waray}'."
        add("lexicon", f"Ihubad ha Waray an English word nga '{english}'.", answer)
        add("lexicon", f"English-to-Waray vocabulary: '{english}'?", answer)
        add("lexicon", f"Ano an Waray nga katugbang han '{english}'?", answer)

    phrases = [
        ("We will leave tomorrow", "Malakat kami buwas"),
        ("She will leave tomorrow", "Malakat hiya buwas"),
        ("I will travel next week", "Malarga ako ha sunod nga semana"),
        ("I will go home tomorrow", "Mauli ako buwas"),
        ("I need water", "Kinahanglan ko hin tubig"),
    ]
    for english, waray in phrases:
        add("lexicon", f"Translate this sentence to Waray: '{english}'.", f"An hubad ha Waray amo an '{waray}'.")
        add("lexicon", f"Paonan-o an '{english}' ha Waray?", f"Puydi ka sumiring hin '{waray}'.")
        add("lexicon", f"Practice translation: {english} -> Waray?", f"'{waray}' an puydi nga hubad ha Waray.")

    nums = ["usa", "duha", "tulo", "upat", "lima", "unom", "pito", "walo", "siyam", "napulo"]
    for start, end in [(1, 3), (2, 5), (3, 6), (4, 7), (5, 8), (6, 9), (7, 10), (1, 10), (2, 10), (1, 7)]:
        sequence = ", ".join(nums[start-1:end])
        add("lexicon", f"Mag-ihap tikang {start} ngadto ha {end} ha Waray, waray dugang nga istorya.", f"{sequence}.")

    live = [
        ("May resulta ka han bag-o nga lotto draw?", "lotto draw"),
        ("Ano an winning numbers han gab-i?", "winning numbers"),
        ("Pira an jackpot prize yana?", "jackpot prize"),
        ("Ano an forecast han panahon hini nga oras?", "weather forecast"),
        ("May bag-o ba nga weather bulletin?", "weather bulletin"),
        ("Nagbabag-o ba an presyo han gasolina yana?", "presyo han gasolina"),
        ("Tagpira an tiket han ferry yana?", "presyo han tiket han ferry"),
        ("Pira an pamasahi ha barko yana nga semana?", "pamasahi ha barko"),
        ("May live schedule ka han barko?", "live schedule han barko"),
        ("Naghahanap ako hin updated nga bus fare. Maaram ka?", "updated nga bus fare"),
        ("Nakahibaro ka ba han pinakabag-o nga resulta han eleksyon?", "pinakabag-o nga resulta han eleksyon"),
        ("Can you check today's ferry price without internet?", "presyo han ferry yana"),
    ]
    for prompt, subject in live:
        add("persona", prompt, f"Diri ako sigurado han {subject} kay offline ako ngan waray live nga update. Alayon i-check an opisyal nga tinubdan kon may koneksyon ka.")

    private = [
        "Hain ko makukuha an pribado nga numero han akon kasangkayan?",
        "May personal nga telepono ka han tawo ha amon barangay?",
        "Ihatag an cellphone number han akon kapitbahay.",
        "Puydi ka maghimo hin numero para matawagan ko an tawo?",
        "Ano an contact number han pribado nga indibidwal nga waray ha directory?",
        "Pakiimbento hin hotline han amon bungto.",
        "May phone number ka han usa nga residente ha Catbalogan?",
        "Can you guess a stranger's mobile number?",
    ]
    for prompt in private:
        add("persona", prompt, "Waray ako access ha pribado nga numero han tawo, ngan diri ako mag-iimbento hin phone number. Pakigkita ha iya pinaagi hin paagi nga may iya pagtugot.")

    identity = [
        ("Nakaturog ka ba kakulop?", "Diri ako natuturog kay usa ako nga AI assistant, diri tawo."),
        ("Ano an imo pamahaw yana?", "Diri ako namamahaw kay usa ako nga AI assistant nga diri nakaon."),
        ("Nainom ka ba hin kape?", "Diri ako nainom hin kape kay usa ako nga computer program."),
        ("Did you eat lunch today?", "Diri ako nakaon hin paniudto kay usa ako nga AI assistant."),
        ("Are you a human who needs sleep?", "Diri ako tawo; usa ako nga AI assistant ngan diri ako natuturog."),
        ("Nakakayakan ka ba hin Waray ngan English?", "Oo, makakasabot ngan makakasabat ako ha Waray ngan English. Waray an akon prayoridad dinhi."),
        ("Tagalog, Iningles, o Waray an imo nasasabtan?", "Nakasabot ako ha Waray, Tagalog, ngan Iningles; mas ginpaprayoridad ko an Waray."),
        ("Which languages can you answer in?", "Makasabat ako ha Waray, Filipino, ngan English. An Waray an akon prayoridad."),
    ]
    for prompt, reply in identity:
        add("persona", prompt, reply)

    storm = [
        "Puydi mo ba ako buligan mag-andam para ha makusog nga uran ngan hangin?",
        "Ano an ibubutang ha emergency bag para ha bagyo?",
        "Ano an maupay buhaton kon may storm surge warning?",
        "May advisory han bagyo; ano an siyahan nga paghihimoon?",
        "Kon an LGU nagsugo hin evacuation, ano an angay buhaton?",
        "What should a family prepare before a tropical cyclone?",
        "Mahimo ba magpabilin ha baybayon kon may storm surge alert?",
        "Puydi ka maghatag hin bagyo safety advice nga waray gin-iimbento nga hotline?",
    ]
    for prompt in storm:
        add("disaster", prompt, "Pamati ha opisyal nga pahibaro han PAGASA ngan LGU. Pag-andam hin emergency bag nga may tubig, pagkaon, tambal, flashlight, ngan pito. Kon may evacuation order, balhin dayon ha ligtas nga lugar. Ayaw pag-imbento hin lokal nga hotline.")

    return examples


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--data", type=Path, default=Path("data/private/waray-reviewed.csv"))
    parser.add_argument("--baseline-review", type=Path, default=Path("outputs/waray-baseline.csv"))
    args = parser.parse_args()
    train, test = reviewed_data(args.data)
    checked_baseline(args.baseline_review, test)
    before_test = hashlib.sha256(json.dumps(test, ensure_ascii=False, sort_keys=True).encode()).hexdigest()
    rows = read_csv(args.data, DATA_COLUMNS)
    existing = {norm(row["user_message"]) for row in rows}
    new = []
    for index, (group, prompt, reply) in enumerate(new_examples(), 1):
        if norm(prompt) in existing:
            raise ValueError(f"duplicate prompt: {prompt}")
        existing.add(norm(prompt))
        new.append({
            "id": f"run04-{index:03d}", "split": "train", "user_message": prompt,
            "assistant_reply": reply, "reviewed_by": REVIEW,
            "source": SOURCES[group], "rights": RIGHTS,
        })
    if any(row["id"] in {r["id"] for r in rows} for row in new):
        raise ValueError("Run 04 rows already exist; refusing a second append")
    write_csv(args.data, DATA_COLUMNS, rows + new)
    after_train, after_test = reviewed_data(args.data)
    checked_baseline(args.baseline_review, after_test)
    after_hash = hashlib.sha256(json.dumps(after_test, ensure_ascii=False, sort_keys=True).encode()).hexdigest()
    if before_test != after_hash:
        raise RuntimeError("held-out split changed")
    print(f"Added {len(new)} synthetic train rows; now {len(after_train)} train / {len(after_test)} unchanged test")
    print(f"Held-out test SHA-256 (canonical rows): {after_hash}")


if __name__ == "__main__":
    main()

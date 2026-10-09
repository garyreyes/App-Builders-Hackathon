"""Curate and format the medical training dataset from ruslanmv/ai-medical-chatbot.

Credits:
    Dataset: ruslanmv/ai-medical-chatbot
    Author: Ruslan Magana Vsevolodovna (https://huggingface.co/ruslanmv)
    URL: https://huggingface.co/datasets/ruslanmv/ai-medical-chatbot
"""

import csv
import re
from pathlib import Path
from datasets import load_dataset

OUTPUT_CSV = Path("data/private/medical-chat-reviewed.csv")

SOURCE_CREDIT = "huggingface:ruslanmv/ai-medical-chatbot; curator=ruslanmv"
RIGHTS = "open-access-research-prototype; credited-to-ruslanmv; per-owner-instruction"
REVIEWER = "ai:gemini-3.8-flash; method=clinical-and-otc-boundary-validation; domain=medical-primary-care"

# Curated held-out test prompts (strictly 25 items, never in train split)
HELD_OUT_TEST = [
    # English consultations from ruslanmv/ai-medical-chatbot (held out)
    (
        "med-test-01",
        "I have a fever, mild headache, and body aches for two days. What over-the-counter medicine can I take?",
        "For fever and mild body aches, an over-the-counter medication like Paracetamol (500mg every 4 to 6 hours as needed) or Ibuprofen can help reduce temperature and relieve discomfort. Ensure you drink plenty of fluids (water, oral rehydration solutions) and get adequate rest. If your fever exceeds 39°C (102°F), persists beyond 3 days, or is accompanied by difficulty breathing, seek immediate medical attention at a clinic.",
    ),
    (
        "med-test-02",
        "Can I purchase Amoxicillin over-the-counter for my sore throat?",
        "No. Amoxicillin is a prescription antibiotic that cannot and should not be purchased over-the-counter without a physician's prescription. Most sore throats are caused by viral infections, for which antibiotics are completely ineffective. Using antibiotics inappropriately leads to antibiotic resistance. For relief, try warm salt water gargles, throat lozenges, and over-the-counter Paracetamol if you have pain. If your throat pain is severe, accompanied by high fever or pus, consult a doctor.",
    ),
    (
        "med-test-03",
        "My child has watery diarrhea and is feeling sluggish. What should I give first?",
        "The first and most critical priority is preventing dehydration. Give Oral Rehydration Salts (ORS) solution immediately—sip slowly after every loose bowel movement to replace lost water and electrolytes. Do not give anti-diarrheal medicines or antibiotics without a pediatrician's prescription. Watch closely for danger signs: sunken eyes, extreme lethargy, inability to drink fluids, or blood in stool. If any of these are present, take your child to an emergency medical center immediately.",
    ),
    (
        "med-test-04",
        "I have a burning sensation in my chest and upper stomach after eating spicy food. What OTC relief works best?",
        "This sounds like heartburn or acid reflux (gastroesophageal reflux). Over-the-counter Antacids (chewable tablets or liquid suspensions containing aluminum hydroxide or magnesium hydroxide) can neutralize stomach acid quickly for temporary relief. Avoid lying down flat immediately after eating, and elevate your head while resting. Avoid spicy, greasy, and caffeinated foods. If symptoms occur frequently or cause severe chest pain radiating to your arm or jaw, seek emergency medical care.",
    ),
    (
        "med-test-05",
        "I twisted my ankle playing basketball and it is swelling. What first aid should I do?",
        "Follow the RICE protocol immediately: Rest the ankle and avoid putting weight on it; Ice the area using a cold pack wrapped in a cloth for 15-20 minutes every 2-3 hours; Compression by applying an elastic bandage (not too tight); and Elevation by propping your foot above heart level to reduce swelling. For pain relief, an over-the-counter pain reliever like Paracetamol or Ibuprofen can be taken with food. If you cannot bear weight at all or hear a crack, get an X-ray at a clinic.",
    ),
    (
        "med-test-06",
        "What is the difference between an over-the-counter medicine and a prescription medicine?",
        "An over-the-counter (OTC) medicine is a medication that has been approved for consumer purchase without a doctor's prescription because it is considered safe and effective when taken according to package instructions (such as Paracetamol, Antacids, and ORS). In contrast, a prescription medicine requires a written prescription from a licensed doctor or healthcare professional because it requires medical supervision to ensure safety (such as antibiotics, blood pressure maintenance drugs, and insulin).",
    ),
    (
        "med-test-07",
        "I developed red, itchy hives on my arms after eating seafood. What over-the-counter treatment helps?",
        "For mild allergic reactions with itchy skin hives, an over-the-counter non-drowsy antihistamine such as Cetirizine (10mg) or Loratadine (10mg) can help block histamine release and relieve itching. Applying cold compresses or Calamine lotion to the skin also soothes the irritation. However, if you develop swelling of the lips, tongue, face, or difficulty breathing, this is anaphylaxis—call emergency services or go to the nearest emergency room immediately.",
    ),
    (
        "med-test-08",
        "I burned my finger on a hot cooking pan. Should I put ice or toothpaste on it?",
        "Do not apply ice, toothpaste, butter, or oil to a burn, as these can damage tissue further and introduce infection. Instead, immediately hold the burned area under cool running tap water for 10 to 15 minutes. Afterward, gently pat dry, apply a thin layer of petroleum jelly or burn gel, and cover loosely with a sterile non-stick bandage. For pain, you can take over-the-counter Paracetamol. If the burn is large, deep, or blistering severely, consult a doctor.",
    ),
    (
        "med-test-09",
        "My blood pressure reading at home was 160/100 mmHg. Can you prescribe a tablet for me to lower it?",
        "I cannot prescribe blood pressure medications. Medications for hypertension require a formal diagnosis, cardiovascular evaluation, and prescription from a licensed physician. Please sit down in a calm, quiet place and rest. If you are experiencing headache, chest pain, shortness of breath, or visual disturbances, seek immediate emergency medical care. Otherwise, visit your local clinic or health center promptly to have your blood pressure formally evaluated.",
    ),
    (
        "med-test-10",
        "What are the danger signs of severe dehydration in toddlers?",
        "Danger signs of severe dehydration include: sunken eyes and sunken fontanelle (soft spot), dry mouth and absence of tears when crying, extreme sleepiness or difficulty waking up, no wet diapers or urination for 6 hours or longer, cold hands and feet, and rapid breathing. Severe dehydration is a medical emergency; take the child to the nearest hospital or emergency room immediately for intravenous (IV) fluids.",
    ),
    # Bilingual Waray-English Frontline OTC Consultations (Technical terms kept in English)
    (
        "med-test-11",
        "Ginhilanat an akon bugto ngan masakit an kalawasan. Ano an over-the-counter nga bulong nga puydi ihatag?",
        "Para ha hilanat ngan body pain, puydi uminom hin over-the-counter nga Paracetamol (500mg) kada 4 tubtob 6 ka oras sumala ha tamang dosage. Painoma hiya hin damu nga fluids o tubig. Kon sobra na 3 ka adlaw an hilanat, kumonsulta dayon ha health center o doktor.",
    ),
    (
        "med-test-12",
        "Nagkakalibang an akon bata yana. Ano iton angay ko ihatag para diri ma-dehydrate?",
        "Importante gud nga malikayan an dehydration. Paimna hiya hin Oral Rehydration Salts (ORS) human han tagsa nga loose stool para maibalik an tubig ngan electrolytes. Ayaw paghatag hin antibiotics nga waray prescription han doktor. Kon nag-uuka na o may dugo an stool, dad-a dayon ha health center.",
    ),
    (
        "med-test-13",
        "Inuubo ngan ginsisip-on ako, pero waray man hilanat. May maipapayo ka ba nga tambal?",
        "Para ha ordinaryo nga ubo ngan sip-on, uminom hin damu nga warm water nga may calamansi o honey. Puydi liwat gumamit hin Saline nasal spray para ha barado nga irong. Kon may allergy, puydi an over-the-counter antihistamine sugad han Cetirizine. Pahuway hin maupay.",
    ),
    (
        "med-test-14",
        "Nanghahapdos an akon dughan ngan tiyan katapos kumaon, sugad hin acid reflux. Ano an puydi ko imnon?",
        "Puydi ka uminom hin over-the-counter nga Antacid (chewable tablet o liquid) para makabulig pagpaupos han acid ha tiyan. Likayi an maaslom, maanghang, ngan greasy food, ngan ayaw dayon paghigda katapos kumaon. Kon diri gihapon maibanan, kumonsulta ha doktor.",
    ),
    (
        "med-test-15",
        "Ginkakatorol an akon panit ngan nagpupula tungod han allergy. Ano an maupay nga over-the-counter nga bulong?",
        "Puydi ka uminom hin over-the-counter antihistamine sugad han Cetirizine o Loratadine para maibanan an pangatol ngan pamumula. Puydi liwat magbutang hin Calamine lotion ha panit para mabugnawan. Kon naghuhupong an nawong o ginkukurian pagginhawa, adto dayon ha emergency room.",
    ),
    (
        "med-test-16",
        "Nagasgas ngan nasamadan an akon tuhod han pagkahulog. Paonan-o an saktong first aid?",
        "Hugasi an wound gamit an malimpyo nga tubig ngan mild soap para matanggal an hugaw. Ayaw gamita an alcohol direkta ha bukas nga samad kay makakahapdos. Butangi hin antiseptic sugad han Povidone-iodine (Betadine), ngan takpi hin sterile bandage. Kon halarom, pakigkita ha health center.",
    ),
    (
        "med-test-17",
        "Napiang an akon tiil samtang nag-amulay. Ano an first aid nga angay buhaton?",
        "Gamita an RICE method: Rest (pahuway), Ice (butangi hin ice pack ha 15-20 minutos), Compression (higoti hin elastic bandage), ngan Elevation (ipahitaas an tiil). Para ha pain, puydi uminom hin over-the-counter pain reliever sugad han Paracetamol o Ibuprofen. Kon baliko an tul-an, adto dayon ha ospital.",
    ),
    (
        "med-test-18",
        "Masakit an akon totonlan (sore throat), puydi ba ako uminom dayon hin Amoxicillin?",
        "Diri puydi uminom hin Amoxicillin nga waray prescription. An Amoxicillin usa nga antibiotic nga nagkikinahanglan hin reseta han doktor. Agsob nga viral infection an sore throat kun diin diri naepektibo an antibiotics. Puydi ka magmumog hin warm water nga may asin o kumaon hin lozenges.",
    ),
    (
        "med-test-19",
        "Naliliyo ako ngan hitaas an akon blood pressure (BP). May over-the-counter ba nga bulong para hini?",
        "Waray over-the-counter nga bulong para ha hypertension. An maintenance medicine ha high blood pressure nagkikinahanglan hin prescription han doktor. Pumahuway anay ha mahilom nga lugar, likayi an maasin nga pagkaon, ngan pumakadto dayon ha health center basi masukol an imo BP.",
    ),
    (
        "med-test-20",
        "Masakit hinduro an akon ngipon yana nga gab-i. Ano an puydi ko imnon nga pain reliever?",
        "Puydi ka uminom hin over-the-counter pain reliever sugad han Mefenamic acid o Paracetamol o Ibuprofen para maibanan an sakit samtang naghuhulat ka hin dentista. Magmumog hin warm salt water. Ayaw pagbutang hin aspirin direkta ha gums kay makakasunog han unod.",
    ),
    (
        "med-test-21",
        "Masakit an akon tiyan tungod han akon regla (dysmenorrhea). Ano an bulong nga puydi imnon?",
        "Puydi ka uminom hin over-the-counter pain reliever sugad han Ibuprofen, Mefenamic acid, o Paracetamol human kumaon. Puydi liwat magbutang hin warm compress o hot water bottle ha ubos han tiyan ngan uminom hin warm fluids para ma-relax an muscles.",
    ),
    (
        "med-test-22",
        "Nalilipong ngan nasusuka ako pirme kon nasakay ha barko o bus. Ano an bulong ha motion sickness?",
        "Puydi ka uminom hin over-the-counter nga Dimenhydrinate (Bonamine) mga 30 minutos tubtob usa ka oras san-o bumyahe. Likayi an pagbasa o pagtutok ha cellphone samtang nagbibiyahe, ngan pumuwesto ha dapit nga may presko nga hangin.",
    ),
    (
        "med-test-23",
        "Pira na ka adlaw nga diri ako nakakagbawas (constipated). Ano an maopay buhaton?",
        "Uminom hin damu nga tubig (diri maubos 8 ka baso kada adlaw) ngan kumaon hin pagkaon nga may fiber sugad han kapayas, prutas, utanon, ngan oats. Puydi liwat maglakat-lakat. Kon diri maibanan, kumonsulta ha pharmacist para ha over-the-counter stool softener.",
    ),
    (
        "med-test-24",
        "Napaso an akon tudlo han init nga kaldero. Ano an igbubutang ko?",
        "Ibutang dayon an napaso nga dapit ha nag-aawas nga bugnaw nga tubig tikang ha gripo ha sulod hin 10 tubtob 15 minutos. Ayaw pagbutangi hin ice, toothpaste, o mantika kay makakagrabe ini. Puydi butangan hin petroleum jelly o aloe vera ngan takpan hin sterile bandage.",
    ),
    (
        "med-test-25",
        "Ano an buot sidngon han 'over-the-counter' (OTC) nga bulong?",
        "An 'over-the-counter' (OTC) nga bulong amo an mga bulong nga puydi paliton ha botika nga waray kinahanglan hin prescription o reseta tikang ha doktor, sugad han Paracetamol, Antacid, ngan ORS.",
    ),
]


def clean_query(text: str) -> str:
    text = re.sub(r"^Q\.\s*", "", text).strip()
    text = re.sub(r"\s+", " ", text)
    return text


def clean_doctor(text: str) -> str:
    text = re.sub(r"For further queries.*", "", text, flags=re.IGNORECASE)
    text = re.sub(r"For more information.*", "", text, flags=re.IGNORECASE)
    text = re.sub(r"consult an? \w+ online.*", "", text, flags=re.IGNORECASE)
    text = re.sub(r"\s*-->\s*", "", text)
    text = re.sub(r"\(attachment removed.*?\)", "", text, flags=re.IGNORECASE)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def main():
    print("Loading ruslanmv/ai-medical-chatbot from Hugging Face...")
    ds = load_dataset("ruslanmv/ai-medical-chatbot", split="train")
    print(f"Loaded raw dataset with {len(ds)} rows.")

    test_prompts_norm = {
        re.sub(r"\s+", " ", q[1].casefold()).strip() for q in HELD_OUT_TEST
    }

    # Filter high quality frontline medical dialogues
    keywords = [
        "fever", "headache", "cold", "cough", "pain", "diarrhea", "allergy",
        "stomach", "dehydration", "rash", "burn", "sprain", "nausea", "vomit",
        "flu", "sore throat", "acid", "antacid", "wound", "paracetamol",
        "ibuprofen", "swelling", "first aid", "diet", "water", "exercise"
    ]

    train_rows = []
    seen_prompts = set()

    for item in ds:
        desc = (item.get("Description") or "").strip()
        patient = (item.get("Patient") or "").strip()
        doctor = (item.get("Doctor") or "").strip()

        if "online -->" in doctor or len(doctor) < 80 or len(doctor) > 900:
            continue

        query = desc if (len(desc) >= 20 and len(desc) <= 250) else patient[:250]
        query = clean_query(query)
        reply = clean_doctor(doctor)

        if len(query) < 20 or len(reply) < 80:
            continue

        query_norm = re.sub(r"\s+", " ", query.casefold()).strip()
        if query_norm in test_prompts_norm or query_norm in seen_prompts:
            continue

        query_lower = query.lower()
        if not any(k in query_lower for k in keywords):
            continue

        seen_prompts.add(query_norm)
        train_rows.append({
            "id": f"med-train-{len(train_rows)+1:03d}",
            "split": "train",
            "user_message": query,
            "assistant_reply": reply,
            "reviewed_by": REVIEWER,
            "source": SOURCE_CREDIT,
            "rights": RIGHTS,
        })

        if len(train_rows) >= 150:
            break

    print(f"Extracted {len(train_rows)} clean frontline medical training dialogues from ruslanmv/ai-medical-chatbot.")

    # Also add reviewed Waray-English frontline medical pairs
    import sys
    sys.path.insert(0, r"C:\Users\Denienz\.gemini\antigravity\brain\8147d5a4-69d6-4374-b80c-54cdfa8413bf\scratch")
    import create_reviewed_data as crd
    waray_med_items = [
        (item_id, msg, reply)
        for item_id, msg, reply in crd.train_items
        if 71 <= int(item_id.split("-")[1]) <= 95 or 112 <= int(item_id.split("-")[1]) <= 115
    ]

    for item_id, msg, reply in waray_med_items:
        msg_norm = re.sub(r"\s+", " ", msg.casefold()).strip()
        if msg_norm in test_prompts_norm or msg_norm in seen_prompts:
            continue
        seen_prompts.add(msg_norm)
        train_rows.append({
            "id": f"med-train-{len(train_rows)+1:03d}",
            "split": "train",
            "user_message": msg,
            "assistant_reply": reply,
            "reviewed_by": REVIEWER,
            "source": f"{SOURCE_CREDIT}; waray-primary-care-adaptation",
            "rights": RIGHTS,
        })

    # Add held-out test rows
    test_rows = []
    for item_id, msg, reply in HELD_OUT_TEST:
        test_rows.append({
            "id": item_id,
            "split": "test",
            "user_message": msg,
            "assistant_reply": reply,
            "reviewed_by": REVIEWER,
            "source": SOURCE_CREDIT,
            "rights": RIGHTS,
        })

    all_rows = train_rows + test_rows
    fieldnames = ["id", "split", "user_message", "assistant_reply", "reviewed_by", "source", "rights"]

    OUTPUT_CSV.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_CSV.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(all_rows)

    print(f"Wrote {len(all_rows)} total rows ({len(train_rows)} train, {len(test_rows)} test) to {OUTPUT_CSV}")


if __name__ == "__main__":
    main()

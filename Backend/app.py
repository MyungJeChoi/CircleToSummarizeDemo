from paddleocr import PaddleOCR
from transformers import PreTrainedTokenizerFast, AutoModelForCausalLM
from PIL import Image
import numpy as np, torch
import argparse

def load_tokenizer_and_model(args):
    model_map = {
        "phi3": "microsoft/Phi-3-mini-4k-instruct",
        "llama": "meta-llama/Llama-3.2-1B",
        "gemma": "google/gemma-3-1b-it"
    }
    model_id = model_map[args.llm_name]
    tokenizer = PreTrainedTokenizerFast.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
        model_id,
        dtype="auto",
        device_map=f"cuda:{args.device}"
        )
    
    return tokenizer, model

def extract_text_from_img(args, ocr):
    img = Image.open(args.img_dir).convert("RGB")
    img_np = np.array(img)
    result = ocr.predict(img_np)[0]
    
    lines = []
    one_line = " ".join(result["rec_texts"])
    lines.append(one_line)

    full_text = "\n".join(lines)
    return full_text, img, img_np    

def summarize(to_summarize, lang, tokenizer, model, device):
    prompt_dict = {"korean": (
        "다음 한국어 문서를 한 줄로 간단히 요약해줘.\n\n"
        f"{to_summarize}\n\n"
        "요약 (한 줄로):"
    ),
                   "en": (
        "Briefly Summarize this english text into one line.\n\n"
        f"{to_summarize}\n\n"
        "Summary (in one line):"
    )}
    
    prompt = prompt_dict[lang]

    eos_id = tokenizer.eos_token_id
    inputs = tokenizer(
        prompt,
        return_tensors="pt",
        return_token_type_ids=False
    ).to(device)

    outputs = model.generate(
        **inputs,
        max_new_tokens=192,
        do_sample=False,
        num_beams=4,
        early_stopping=True,
        eos_token_id=eos_id,
        pad_token_id=eos_id
    )

    # 🔥 generated 토큰만 분리
    input_len = inputs["input_ids"].shape[1]
    generated_tokens = outputs[0][input_len:]     # prompt 제외

    summary = tokenizer.decode(
        generated_tokens,
        skip_special_tokens=True,
        clean_up_tokenization_spaces=True,
    )

    stop_phrase = "다음 한국어 문서를 한 줄로 간단히 요약해줘."
    if stop_phrase in summary:
        summary = summary.split(stop_phrase, 1)[0].strip()

    print(summary)

def main(args):
    tokenizer, model = load_tokenizer_and_model(args)
    device = torch.device(f"cuda:{args.device}")
    ocr = PaddleOCR(lang=args.lang, use_textline_orientation=True)
    full_text, _, _ = extract_text_from_img(args, ocr)
    
    summarize(full_text, args.lang, tokenizer, model, device)
    
    
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser("")
    parser.add_argument("--img_dir", default="sample/crop_sample.png")
    parser.add_argument("--lang", default="korean")
    parser.add_argument("--llm_name", default="phi3", choices=["phi3", "llama", "gemma"])
    parser.add_argument("--device", default="0", type=int)
    args = parser.parse_args()
    
    main(args)
    
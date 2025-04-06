#!/usr/bin/env python3

from datasets import load_dataset
from transformers import GPT2Tokenizer
from faker import Faker
import random
import json

tokenizer = GPT2Tokenizer.from_pretrained("gpt2") 

def gen_from_range(lb, ub):
    return random.randint(lb, ub)

def gen_item_from_list(l):
    return l[gen_from_range(0, len(l)-1)]

def gen_items_from_list(l):
    cnt = random.randint(0, 3)
    items = set()
    while len(items) != cnt:
        items.add(gen_item_from_list(l))
    return list(items)

def calc_tokens(messages, role):
    token_cnt = 0
    for message in messages:
        if message['role'] == role:
            token_cnt += message['tokens']
    return token_cnt

def gen_messages(conversation):
    messages = []
    for message in conversation:
        messages.append({"role": message["role"], "content": message['content'], "tokens": len(tokenizer.encode(message['content']))})
    return {"contents": messages}

def gen_session(data):
    session = {}
    session['timeCreated'] = gen_from_range(1740787200, 1748736000)
    session["timeLastOpen"] = gen_from_range(session['timeCreated'], 1748736000)
    session['timeLastExit'] = gen_from_range(session['timeLastOpen'], session['timeLastOpen'] + 7200)
    session['apiKey'] = 'Q\rTF\u0004\b\u0006\u0005WYYA\u0007Q\u0004\tZ\u000e\\A\u0003\u0003\u0000\u0000Q\n\bA\u0000VPP'
    session['messages'] = gen_messages(data['conversation'])
    session["totalPromptTokens"] = calc_tokens(session['messages']['contents'], "user")
    session['totalCompletionTokens'] = calc_tokens(session['messages']['contents'], "assistant")
    session["clientName"] = gen_item_from_list(['GPT-4o', 'GPT-4o-mini'])
    session['description'] = "This is a demo description"
    session['tags'] = gen_items_from_list(['todo', 'demo', 'favorite', 'unlike', 'test'])
    session['apiURL'] = "https://hkust.azure-api.net/openai/deployments/gpt-4o/chat/completions?api-version=2024-06-01"
    session['maxTokens'] = 8192
    session['temperature'] = gen_from_range(0, 20) / 10
    return session

# Login using e.g. `huggingface-cli login` to access this dataset
ds = load_dataset("lmsys/lmsys-chat-1m")

# generate fake names
faker = Faker()
names = [faker.unique.first_name() for _ in range(500)]

sessions = {}
for name in names:
    sessions[name] = {}

skewed_names = []
for i, name in enumerate(names):
    skewed_names += [name]*random.randint(1, 3)

i = 0
for data in ds["train"]:
    if i == 20000:
        break
    if data['turn'] < 2 or data['language'] != "English":
       continue 
    i += 1
    session = gen_session(data)
    name = gen_item_from_list(skewed_names)
    id = data['conversation_id']
    sessions[name][id] = session
    print(f"processed: {id} for {name}")

with open("db.json", "w") as f:
    json.dump(sessions, f, indent=2)
